package com.rev.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @JsonProperty("name")
    private String name;

    @JsonProperty("username")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @JsonProperty("password")
    private String password;

    @JsonProperty("masterPassword")
    private String masterPassword;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
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