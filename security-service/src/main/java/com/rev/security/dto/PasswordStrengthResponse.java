package com.rev.security.dto;

public class PasswordStrengthResponse {

    private String strength;

    public PasswordStrengthResponse(String strength) {
        this.strength = strength;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }
}