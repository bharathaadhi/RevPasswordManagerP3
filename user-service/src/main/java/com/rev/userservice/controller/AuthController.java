package com.rev.userservice.controller;
import com.rev.userservice.dto.*;
import com.rev.userservice.model.User;
import com.rev.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.rev.userservice.dto.TwoFactorRequest;
import com.rev.userservice.dto.RefreshTokenRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return new LoginResponse(token);
    }
    @GetMapping("/profile")
    public String profile() {
        return "Protected profile accessed successfully";
    }
    @PostMapping("/security-question")
    public String saveSecurityQuestion(@RequestBody SecurityQuestionRequest request) {
        return userService.saveSecurityQuestion(request);
    }
    @PostMapping("/recover")
    public String recoverAccount(@RequestBody SecurityQuestionRequest request) {
        return userService.recoverAccount(request);
    }
    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(request);
    }
    @PostMapping("/generate-2fa")
    public String generate2FA(@RequestParam String email) {
        return userService.generate2FACode(email);
    }
    @PostMapping("/verify-2fa")
    public String verify2FA(@RequestBody TwoFactorRequest request) {
        return userService.verify2FACode(request);
    }
    @PostMapping("/refresh-token")
    public String refreshToken(@RequestBody RefreshTokenRequest request) {
        return userService.refreshToken(request);
    }
}