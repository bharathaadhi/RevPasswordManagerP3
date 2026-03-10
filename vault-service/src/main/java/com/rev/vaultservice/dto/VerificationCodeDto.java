package com.rev.vaultservice.dto;

import lombok.Data;

@Data
public class VerificationCodeDto {
    private String code;
    private String masterPassword;
    private String email;
}