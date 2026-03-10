package com.rev.userservice.service;
import com.rev.userservice.dto.*;
import com.rev.userservice.entity.SecurityQuestion;
import com.rev.userservice.model.User;
import com.rev.userservice.repository.UserRepository;
import com.rev.userservice.repository.SecurityQuestionRepository;
import com.rev.userservice.security.JwtUtil;
import com.rev.userservice.entity.TwoFactorCode;
import com.rev.userservice.repository.TwoFactorCodeRepository;
import com.rev.userservice.entity.RefreshToken;
import com.rev.userservice.repository.RefreshTokenRepository;
import com.rev.userservice.client.NotificationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;
    @Autowired
    private TwoFactorCodeRepository twoFactorCodeRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private NotificationClient notificationClient;

    @Override
    public User register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getEffectiveName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getEffectivePassword()));
        user.setPhone(request.getPhone());
        User savedUser = userRepository.save(user);

        // Save security questions if provided
        if (request.getSecurityQuestions() != null) {
            for (RegisterRequest.SecurityQuestionItem sq : request.getSecurityQuestions()) {
                SecurityQuestion entity = new SecurityQuestion();
                entity.setEmail(request.getEmail());
                entity.setQuestion(sq.getQuestion());
                entity.setAnswer(sq.getAnswer());
                securityQuestionRepository.save(entity);
            }
        }

        return savedUser;
    }

    @Override
    public String login(LoginRequest request) {
        String identifier = request.getEffectiveEmail();
        String password = request.getEffectivePassword();

        // Try to find by email first, then by name (username)
        Optional<User> user = userRepository.findFirstByEmail(identifier);
        if (user.isEmpty()) {
            user = userRepository.findFirstByName(identifier);
        }

        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            safeSendNotification(user.get().getEmail(), "New Login Detected", "A new login was detected on your account.", "SECURITY");
            return jwtUtil.generateToken(user.get().getEmail());
        }
        throw new RuntimeException("Invalid email or password");
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String saveSecurityQuestion(SecurityAnswerUpdateDto request) {
        String identifier = request.getUsernameOrEmail();
        Optional<User> userOpt = userRepository.findFirstByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findFirstByName(identifier);
        }
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getSecurityQuestions() == null || request.getSecurityQuestions().isEmpty()) {
            throw new RuntimeException("No security questions provided");
        }

        // Delete existing questions for this user
        securityQuestionRepository.deleteByEmail(user.getEmail());

        // Save new ones
        for (SecurityAnswerUpdateDto.SecurityQuestionItem item : request.getSecurityQuestions()) {
            SecurityQuestion sq = new SecurityQuestion();
            sq.setEmail(user.getEmail());
            sq.setQuestion(item.getQuestion());
            sq.setAnswer(item.getAnswer());
            securityQuestionRepository.save(sq);
        }

        return "Security questions updated";
    }

    @Override
    public String recoverAccount(ForgotPasswordDto request) {
        String identifier = request.getUsernameOrEmail();
        Optional<User> userOpt = userRepository.findFirstByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findFirstByName(identifier);
        }
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found: " + identifier));

        java.util.List<SecurityQuestion> dbQuestions = securityQuestionRepository.findAllByEmail(user.getEmail());
        if (dbQuestions.isEmpty()) {
            throw new RuntimeException("No security questions found for user");
        }

        boolean atLeastOneCorrect = false;
        if (request.getSecurityQuestions() != null) {
            for (ForgotPasswordDto.SecurityQuestionItem reqSq : request.getSecurityQuestions()) {
                for (SecurityQuestion dbSq : dbQuestions) {
                    if (dbSq.getQuestion().equalsIgnoreCase(reqSq.getQuestion()) &&
                        dbSq.getAnswer().equalsIgnoreCase(reqSq.getAnswer())) {
                        atLeastOneCorrect = true;
                        break;
                    }
                }
                if (atLeastOneCorrect) break;
            }
        }

        if (!atLeastOneCorrect) {
            throw new RuntimeException("Security questions answered incorrectly.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        safeSendNotification(user.getEmail(), "Password Reset", "Your master password was reset successfully.", "SECURITY");

        return "Password reset successful";
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findFirstByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password reset successful";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String verify2FACode(TwoFactorRequest request) {
        String email = request.getEmail() != null ? request.getEmail().toLowerCase().trim() : "";
        String inputCode = request.getCode() != null ? request.getCode().trim() : "";
        
        System.out.println("[2FA-VERIFY] Attempting verification for: " + email + " with code: " + inputCode);

        // Always get the most recent code saved for this email
        TwoFactorCode codeEntity = twoFactorCodeRepository.findFirstByEmailOrderByIdDesc(email)
                .orElse(null);
        
        if (codeEntity == null) {
            System.out.println("[2FA-VERIFY] CRITICAL: No code found in DB for " + email);
            return "ERROR: Verification code not found for " + email + ". Please request a new one.";
        }

        System.out.println("[2FA-VERIFY] Found code in DB: '" + codeEntity.getCode() + "' (Stored Email: " + codeEntity.getEmail() + ")");

        if (codeEntity.getCode().trim().equals(inputCode)) {
            System.out.println("[2FA-VERIFY] SUCCESS - Codes match");
            twoFactorCodeRepository.deleteByEmail(email);
            twoFactorCodeRepository.flush();
            return "2FA verification successful";
        }
        
        System.out.println("[2FA-VERIFY] FAILURE - Input '" + inputCode + "' does NOT match DB '" + codeEntity.getCode() + "'");
        return "Invalid 2FA code. Please check your email and try again.";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String generate2FACode(String email) {
        String normalizedEmail = email != null ? email.toLowerCase().trim() : "";
        System.out.println("[2FA-GENERATE] Request for: " + normalizedEmail);
        
        // Clear previous codes and flush immediately
        twoFactorCodeRepository.deleteByEmail(normalizedEmail);
        twoFactorCodeRepository.flush();

        String generatedCode = String.valueOf(100000 + (int)(Math.random() * 900000));
        TwoFactorCode code = new TwoFactorCode();
        code.setEmail(normalizedEmail);
        code.setCode(generatedCode);
        
        TwoFactorCode saved = twoFactorCodeRepository.saveAndFlush(code);
        
        System.out.println("[2FA-GENERATE] Generated code " + generatedCode + " for " + normalizedEmail + " (ID: " + saved.getId() + ")");
        return "2FA code generated: " + generatedCode;
    }

    @Override
    public User updateProfile(String email, RegisterRequest request) {
        User user = userRepository.findFirstByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(request.getEffectiveName());
        if (request.getEffectivePassword() != null && !request.getEffectivePassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getEffectivePassword()));
        }
        return userRepository.save(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String logout(String email) {
        refreshTokenRepository.deleteByEmail(email);
        twoFactorCodeRepository.deleteByEmail(email);
        return "Logged out successfully";
    }

    @Override
    public boolean verifyMasterPassword(MasterPasswordDto dto) {
        String identifier = dto.getEmail();
        Optional<User> user = userRepository.findFirstByEmail(identifier);
        if (user.isEmpty()) {
            user = userRepository.findFirstByName(identifier);
        }
        
        return user.isPresent() && passwordEncoder.matches(dto.getMasterPassword(), user.get().getPassword());
    }

    @Override
    public String refreshToken(RefreshTokenRequest request) {
        String newToken = jwtUtil.generateToken(request.getEmail());
        RefreshToken refresh = new RefreshToken();
        refresh.setEmail(request.getEmail());
        refresh.setToken(newToken);
        refreshTokenRepository.save(refresh);
        return newToken;
    }
    @Override
    public String updateMasterPassword(UpdateMasterPasswordRequest request) {
        String identifier = request.getUsernameOrEmail();
        Optional<User> userOpt = userRepository.findFirstByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findFirstByName(identifier);
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current master password incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        safeSendNotification(user.getEmail(), "Password Updated", "Your master password was just updated from the profile settings.", "SECURITY");

        return "Master password updated successfully";
    }

    private void safeSendNotification(String email, String title, String msg, String type) {
        try {
            notificationClient.sendNotification(Map.of(
                    "recipientEmail", email,
                    "title", title,
                    "message", msg,
                    "type", type
            ));
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}