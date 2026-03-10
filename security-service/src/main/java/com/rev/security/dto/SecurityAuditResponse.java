package com.rev.security.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityAuditResponse {
    private int weakPasswords;
    private int reusedPasswords;
    private int leakedPasswords;
    private List<String> weakPasswordList;
    private List<String> leakedPasswordList;
    private String overallStatus;
}