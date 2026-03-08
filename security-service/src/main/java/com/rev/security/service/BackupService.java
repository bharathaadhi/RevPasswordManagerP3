package com.rev.security.service;

import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.List;

@Service
public class BackupService {

    public String exportBackup(List<String> passwords) {

        String combined = String.join(",", passwords);

        return Base64.getEncoder().encodeToString(combined.getBytes());
    }
}