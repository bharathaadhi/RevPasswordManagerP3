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

            if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[0-9].*")) {
                weakPasswords.add(password);
            }
        }

        return weakPasswords;
    }

    public com.rev.security.dto.SecurityAuditResponse generateAuditReport(List<String> passwords) {
        int weakCount = countWeakPasswords(passwords);
        int reusedCount = countReusedPasswords(passwords);
        List<String> weakList = findWeakPasswords(passwords);
        
        String overallStatus = (weakCount == 0 && reusedCount == 0) ? "Excellent" : (weakCount < 2 ? "Good" : "Warning");
        
        // Match the constructor in SecurityAuditResponse.java:
        // int weakPasswords, int reusedPasswords, int leakedPasswords, List<String> weakPasswordList, List<String> leakedPasswordList, String overallStatus
        return new com.rev.security.dto.SecurityAuditResponse(weakCount, reusedCount, 0, weakList, new ArrayList<>(), overallStatus);
    }
}