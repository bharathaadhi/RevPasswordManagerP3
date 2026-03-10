package com.rev.vaultservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BackupRequestDto {
    private Long userId;
    private String email;
    private String masterPassword;
    private String code;
    private List<VaultRequestDto> entries;
}
