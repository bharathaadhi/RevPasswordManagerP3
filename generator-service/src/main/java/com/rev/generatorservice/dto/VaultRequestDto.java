package com.rev.generatorservice.dto;

import lombok.Data;

@Data
public class VaultRequestDto {

    private Long userId;
    private String platform;
    private String username;
    private String encryptedPassword;
    private String category;
    private boolean favorite;
}