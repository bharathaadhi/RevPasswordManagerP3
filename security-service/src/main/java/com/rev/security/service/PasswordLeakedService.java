package com.rev.security.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class PasswordLeakedService {

    // Simulated list of leaked passwords
    private static final Set<String> LEAKED_PASSWORDS = new HashSet<>(Arrays.asList(
        "password123", "admin", "123456", "12345678", "qwerty", "password", 
        "welcome", "login123", "admin123", "pass@123", "123123", "sunshine"
    ));

    public boolean isLeaked(String password) {
        if (password == null) return false;
        return LEAKED_PASSWORDS.contains(password.toLowerCase());
    }
}
