package com.rev.security.dto;

public class PasswordReuseResponse {

    private boolean reused;

    public PasswordReuseResponse(boolean reused) {
        this.reused = reused;
    }

    public boolean isReused() {
        return reused;
    }

    public void setReused(boolean reused) {
        this.reused = reused;
    }
}