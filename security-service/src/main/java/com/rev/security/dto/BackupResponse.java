package com.rev.security.dto;

public class BackupResponse {

    private String backup;

    public BackupResponse(String backup) {
        this.backup = backup;
    }

    public String getBackup() {
        return backup;
    }

    public void setBackup(String backup) {
        this.backup = backup;
    }
}