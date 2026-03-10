package com.rev.security.service;

import org.springframework.stereotype.Service;

@Service
public class PasswordStrengthService {

    public String checkStrength(String password) {

        if (password.length() < 6) {
            return "WEAK";
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[@$!%*?&].*");

        if (hasUpper && hasLower && hasNumber && hasSpecial) {
            return "STRONG";
        }

        return "MEDIUM";
    }
}