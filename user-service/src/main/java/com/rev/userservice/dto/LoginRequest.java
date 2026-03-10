package com.rev.userservice.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String usernameOrEmail;  // frontend sends this
    private String masterPassword;   // frontend sends this

    // Helper: get the effective email/username
    public String getEffectiveEmail() {
        if (usernameOrEmail != null && !usernameOrEmail.isEmpty()) {
            return usernameOrEmail;
        }
        return email;
    }

    // Helper: get the effective password
    public String getEffectivePassword() {
        if (masterPassword != null && !masterPassword.isEmpty()) {
            return masterPassword;
        }
        return password;
    }
}