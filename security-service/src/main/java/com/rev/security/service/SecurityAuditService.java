package com.rev.security.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SecurityAuditService {

    public int countWeakPasswords(List<String> passwords) {

        int weakCount = 0;

        for (String password : passwords) {

            if (password.length() < 6) {
                weakCount++;
            }
        }

        return weakCount;
    }

    public int countReusedPasswords(List<String> passwords) {

        Set<String> unique = new HashSet<>();
        int reused = 0;

        for (String password : passwords) {

            if (!unique.add(password)) {
                reused++;
            }
        }

        return reused;
    }

    public List<String> findWeakPasswords(List<String> passwords) {

        List<String> weakPasswords = new ArrayList<>();

        for (String password : passwords) {

            if (password.length() < 6) {
                weakPasswords.add(password);
            }
        }

        return weakPasswords;
    }
}