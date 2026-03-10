package com.rev.userservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class RegisterRequest {
    private String name;
    private String username;   // frontend sends 'username'
    private String email;
    private String password;
    private String masterPassword;  // frontend sends 'masterPassword'
    private String phone;
    private List<SecurityQuestionItem> securityQuestions;

    // Helper: get the actual name (frontend sends 'username')
    public String getEffectiveName() {
        return name != null ? name : username;
    }

    // Helper: get the actual password (frontend sends 'masterPassword')
    public String getEffectivePassword() {
        return password != null ? password : masterPassword;
    }

    @Data
    public static class SecurityQuestionItem {
        private String question;
        private String answer;
    }
}