package com.rev.userservice.controller;

import com.rev.userservice.dto.*;
import com.rev.userservice.model.User;
import com.rev.userservice.repository.UserRepository;
import com.rev.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // ============ REGISTER / LOGIN / LOGOUT ============

    @PostMapping("/register")
    public ResponseEntity<?> register(@jakarta.validation.Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.login(request);
            String identifier = request.getIdentifier() != null ? request.getIdentifier().trim() : "";
            
            // Try to find by email first (normalized lowercase), then by name
            User user = userRepository.findFirstByEmail(identifier.toLowerCase()).orElse(null);
            if (user == null) {
                user = userRepository.findFirstByName(identifier).orElse(null);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", user != null ? user.getEmail() : identifier.toLowerCase());
            response.put("username", user != null ? user.getName() : identifier);
            response.put("twoFactorEnabled", user != null && user.isTwoFactorEnabled());
            response.put("twoFactorRequired", user != null && user.isTwoFactorEnabled());
            
            if (user != null) {
                response.put("name", user.getName());
                response.put("userId", user.getId());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.logout(email));
    }

    // ============ PROFILE ============

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam("usernameOrEmail") String usernameOrEmail) {
        String normalized = usernameOrEmail != null ? usernameOrEmail.toLowerCase().trim() : "";
        Optional<User> userOpt = userRepository.findFirstByEmail(normalized);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findFirstByName(usernameOrEmail);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found: " + usernameOrEmail));
        }
        User user = userOpt.get();
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("twoFactorEnabled", user.isTwoFactorEnabled());
        profile.put("userId", user.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> payload) {
        
        Long userId = null;
        if (payload.containsKey("userId")) {
            Object idObj = payload.get("userId");
            if (idObj instanceof Number) {
                userId = ((Number) idObj).longValue();
            }
        }
        
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User ID required for update"));
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found with ID: " + userId));
        }

        User user = userOpt.get();
        
        // Name is fixed, ONLY update email and phone
        if (payload.containsKey("email")) {
            String newEmail = (String) payload.get("email");
            if (newEmail == null || !newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid email format"));
            }
            // Basic check if email already exists for another user
            Optional<User> existing = userRepository.findFirstByEmail(newEmail.toLowerCase());
            if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email already in use by another user"));
            }
            user.setEmail(newEmail.toLowerCase().trim());
        }
        
        if (payload.containsKey("phone")) {
            String newPhone = (String) payload.get("phone");
            if (newPhone == null || !newPhone.matches("^\\d{10}$")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Phone number must be exactly 10 digits"));
            }
            user.setPhone(newPhone);
        }
        
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    // ============ SECURITY QUESTIONS ============

    @PostMapping("/security-question")
    public ResponseEntity<Map<String, String>> saveSecurityQuestion(@RequestBody SecurityAnswerUpdateDto request) {
        try {
            String msg = userService.saveSecurityQuestion(request);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping({"/security-questions", "/security_questions"})
    public ResponseEntity<List<String>> getSecurityQuestions(@RequestParam("usernameOrEmail") String usernameOrEmail) {
        return ResponseEntity.ok(userService.getSecurityQuestions(usernameOrEmail));
    }

    // ============ PASSWORD RECOVERY ============

    @PostMapping("/recover")
    public ResponseEntity<Map<String, String>> recoverAccount(@RequestBody ForgotPasswordDto request) {
        try {
            String msg = userService.recoverAccount(request);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }

    @PostMapping("/update-master-password")
    public ResponseEntity<String> updateMasterPassword(@RequestBody UpdateMasterPasswordRequest request) {
        try {
            return ResponseEntity.ok(userService.updateMasterPassword(request));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // ============ 2FA ============

    @PostMapping("/generate-2fa")
    public ResponseEntity<String> generate2FA(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.generate2FACode(email));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<String> verify2FA(@RequestBody TwoFactorRequest request) {
        String result = userService.verify2FACode(request);
        if ("2FA verification successful".equals(result)) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/2fa-status")
    public ResponseEntity<Map<String, Object>> get2FAStatus(@RequestParam("usernameOrEmail") String usernameOrEmail) {
        String normalized = usernameOrEmail != null ? usernameOrEmail.toLowerCase().trim() : "";
        Optional<User> userOpt = userRepository.findFirstByEmail(normalized);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findFirstByName(usernameOrEmail);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", userOpt.map(User::isTwoFactorEnabled).orElse(false));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/toggle-2fa")
    public ResponseEntity<Map<String, Object>> toggle2FA(
            @RequestParam("usernameOrEmail") String usernameOrEmail,
            @RequestParam("enabled") boolean enabled) {
        String normalized = usernameOrEmail != null ? usernameOrEmail.toLowerCase().trim() : "";
        Optional<User> userOpt = userRepository.findFirstByEmail(normalized);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findFirstByName(usernameOrEmail);
        }
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        User user = userOpt.get();
        user.setTwoFactorEnabled(enabled);
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("enabled", enabled);
        result.put("message", enabled ? "2FA enabled" : "2FA disabled");
        return ResponseEntity.ok(result);
    }

    // ============ MASTER PASSWORD VERIFICATION ============

    @PostMapping("/verify-master")
    public ResponseEntity<Boolean> verifyMasterPassword(@RequestBody MasterPasswordDto dto) {
        return ResponseEntity.ok(userService.verifyMasterPassword(dto));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(userService.refreshToken(request));
    }
}