package com.rev.userservice.dto;

import lombok.Data;

@Data
public class LoginRequest {

    // Login identifier
    private String email;
    private String username;
    private String usernameOrEmail;

    // Password
    private String password;
    private String masterPassword;

    // Get the identifier (email OR username)
    public String getIdentifier() {

        if (usernameOrEmail != null && !usernameOrEmail.isBlank()) {
            return usernameOrEmail;
        }

        if (email != null && !email.isBlank()) {
            return email;
        }

        if (username != null && !username.isBlank()) {
            return username;
        }

        return null;
    }

    // Get the password (masterPassword preferred)
    public String getPasswordValue() {

        if (masterPassword != null && !masterPassword.isBlank()) {
            return masterPassword;
        }

        return password;
    }
}