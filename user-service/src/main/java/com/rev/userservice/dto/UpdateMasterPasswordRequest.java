package com.rev.userservice.dto;

import lombok.Data;

@Data
public class UpdateMasterPasswordRequest {
    private String usernameOrEmail;
    private String currentPassword;
    private String newPassword;
}
