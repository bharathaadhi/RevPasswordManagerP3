package com.rev.security.dto;

public class SecurityAuditResponse {

    private int totalPasswords;
    private int weakPasswords;
    private int reusedPasswords;

    public SecurityAuditResponse(int totalPasswords, int weakPasswords, int reusedPasswords) {
        this.totalPasswords = totalPasswords;
        this.weakPasswords = weakPasswords;
        this.reusedPasswords = reusedPasswords;
    }

    public int getTotalPasswords() {
        return totalPasswords;
    }

    public int getWeakPasswords() {
        return weakPasswords;
    }

    public int getReusedPasswords() {
        return reusedPasswords;
    }
}