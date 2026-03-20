package com.rev.vaultservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterPasswordDto {
    private String masterPassword;
    private String email;
    private String code;

    public MasterPasswordDto(String masterPassword, String email) {
        this.masterPassword = masterPassword;
        this.email = email;
    }
}