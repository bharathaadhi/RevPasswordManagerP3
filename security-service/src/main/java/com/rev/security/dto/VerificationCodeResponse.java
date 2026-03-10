package com.rev.security.dto;

public class VerificationCodeResponse {

    private String code;

    public VerificationCodeResponse(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}