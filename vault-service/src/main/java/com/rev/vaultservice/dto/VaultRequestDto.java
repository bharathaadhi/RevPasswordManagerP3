package com.rev.vaultservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaultRequestDto {

    private Long userId;
    private String platform;
    private String username;
    private String encryptedPassword;
    private String category;
    private boolean favorite;
    private String strength;
    private boolean alreadyEncrypted;
}