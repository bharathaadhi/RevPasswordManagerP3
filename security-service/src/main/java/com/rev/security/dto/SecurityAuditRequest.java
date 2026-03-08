package com.rev.security.dto;

import java.util.List;

public class SecurityAuditRequest {

    private List<String> passwords;

    public List<String> getPasswords() {
        return passwords;
    }

    public void setPasswords(List<String> passwords) {
        this.passwords = passwords;
    }
}