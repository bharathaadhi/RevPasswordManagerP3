package com.rev.security.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PasswordReuseService {

    public boolean isPasswordReused(List<String> passwords) {

        Set<String> uniquePasswords = new HashSet<>();

        for (String password : passwords) {

            if (uniquePasswords.contains(password)) {
                return true;
            }

            uniquePasswords.add(password);
        }

        return false;
    }
}