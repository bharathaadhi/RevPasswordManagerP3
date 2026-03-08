package com.rev.userservice.service;

import com.rev.userservice.dto.ResetPasswordRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.rev.userservice.dto.RegisterRequest;
import com.rev.userservice.model.User;
import com.rev.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rev.userservice.dto.LoginRequest;
import java.util.Optional;
import com.rev.userservice.dto.SecurityQuestionRequest;
import com.rev.userservice.entity.SecurityQuestion;
import com.rev.userservice.repository.SecurityQuestionRepository;
import com.rev.userservice.security.JwtUtil;
import com.rev.userservice.dto.TwoFactorRequest;
import com.rev.userservice.entity.TwoFactorCode;
import com.rev.userservice.repository.TwoFactorCodeRepository;
import com.rev.userservice.dto.RefreshTokenRequest;
import com.rev.userservice.entity.RefreshToken;
import com.rev.userservice.repository.RefreshTokenRepository;
import com.rev.userservice.feign.NotificationClient;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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

    private final NotificationClient notificationClient;

    public UserServiceImpl(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Override
    public User register(RegisterRequest request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        Map<String,String> notification = new HashMap<>();

        notification.put("email", request.getEmail());
        notification.put("subject", "Welcome to Rev Password Manager");
        notification.put("message", "Your account has been successfully created.");

        notificationClient.sendNotification(notification);

        return savedUser;
    }

    @Override
    public String login(LoginRequest request) {

        Optional<User> user = userRepository.findByEmail(request.getEmail());

        if (user.isPresent() && passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {

            String token = jwtUtil.generateToken(user.get().getEmail());

            Map<String,String> notification = new HashMap<>();

            notification.put("email", request.getEmail());
            notification.put("subject", "New Login Detected");
            notification.put("message", "A new login was detected in your account.");

            notificationClient.sendNotification(notification);

            return token;
        }

        throw new RuntimeException("Invalid email or password");
    }

    @Override
    public String saveSecurityQuestion(SecurityQuestionRequest request) {

        SecurityQuestion sq = new SecurityQuestion();

        sq.setEmail(request.getEmail());
        sq.setQuestion(request.getQuestion());
        sq.setAnswer(request.getAnswer());

        securityQuestionRepository.save(sq);

        return "Security question saved";
    }

    @Override
    public String recoverAccount(SecurityQuestionRequest request) {

        SecurityQuestion sq = securityQuestionRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (sq.getAnswer().equalsIgnoreCase(request.getAnswer())) {
            return "Recovery successful";
        }

        return "Wrong answer";
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        Map<String,String> notification = new HashMap<>();

        notification.put("email", request.getEmail());
        notification.put("subject", "Password Changed Successfully");
        notification.put("message", "Your password has been changed. If this was not you, please secure your account immediately.");

        notificationClient.sendNotification(notification);

        return "Password reset successful";
    }
    @Override
    public String verify2FACode(TwoFactorRequest request) {

        TwoFactorCode code = twoFactorCodeRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No code found"));

        if (code.getCode().equals(request.getCode())) {
            return "2FA verification successful";
        }

        return "Invalid 2FA code";
    }

    @Override
    public String generate2FACode(String email) {

        TwoFactorCode code = new TwoFactorCode();

        code.setEmail(email);
        code.setCode("123456");

        twoFactorCodeRepository.save(code);

        Map<String,String> notification = new HashMap<>();

        notification.put("email", email);
        notification.put("subject", "2FA Verification Code");
        notification.put("message", "Your verification code is: 123456");

        notificationClient.sendNotification(notification);

        return "2FA code generated: 123456";
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
}