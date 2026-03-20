package com.rev.userservice.service;
import com.rev.userservice.dto.*;
import com.rev.userservice.model.User;
import com.rev.userservice.dto.RefreshTokenRequest;
public interface UserService {
    User register(RegisterRequest request);

    String login(LoginRequest request);

    String saveSecurityQuestion(SecurityAnswerUpdateDto request);

    String recoverAccount(ForgotPasswordDto request);

    String resetPassword(ResetPasswordRequest request);

    String generate2FACode(String email);

    String verify2FACode(TwoFactorRequest request);

    String refreshToken(RefreshTokenRequest request);
    User updateProfile(String email, RegisterRequest request);
    String logout(String email);
    boolean verifyMasterPassword(MasterPasswordDto dto);
    String updateMasterPassword(UpdateMasterPasswordRequest request);
    java.util.List<String> getSecurityQuestions(String usernameOrEmail);
}