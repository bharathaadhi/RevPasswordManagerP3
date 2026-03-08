package com.rev.security.dto;

import java.util.List;

public class WeakPasswordResponse {

    private List<String> weakPasswords;

    public WeakPasswordResponse(List<String> weakPasswords) {
        this.weakPasswords = weakPasswords;
    }

    public List<String> getWeakPasswords() {
        return weakPasswords;
    }

    public void setWeakPasswords(List<String> weakPasswords) {
        this.weakPasswords = weakPasswords;
    }
}