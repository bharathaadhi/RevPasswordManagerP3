package com.rev.security.controller;

import com.rev.security.dto.*;
import com.rev.security.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/security")
public class SecurityController {

    @Autowired
    private PasswordStrengthService passwordStrengthService;

    @PostMapping("/check-strength")
    public PasswordStrengthResponse checkStrength(@RequestBody PasswordRequest request) {

        String result = passwordStrengthService.checkStrength(request.getPassword());

        return new PasswordStrengthResponse(result);
    }

    @Autowired
    private PasswordLeakedService passwordLeakedService;

    @PostMapping("/check-leaked")
    public boolean checkLeaked(@RequestBody PasswordRequest request) {
        return passwordLeakedService.isLeaked(request.getPassword());
    }

    @PostMapping("/check-leaked-batch")
    public List<Boolean> checkLeakedBatch(@RequestBody List<String> passwords) {
        return passwords.stream().map(passwordLeakedService::isLeaked).collect(Collectors.toList());
    }

    @Autowired
    private VerificationCodeService verificationCodeService;

    @PostMapping("/generate-code")
    public VerificationCodeResponse generateCode() {

        String code = verificationCodeService.generateCode();

        return new VerificationCodeResponse(code);
    }

    @Autowired
    private PasswordReuseService passwordReuseService;

    @PostMapping("/check-reuse")
    public PasswordReuseResponse checkReuse(@RequestBody PasswordReuseRequest request) {

        boolean reused = passwordReuseService.isPasswordReused(request.getPasswords());

        return new PasswordReuseResponse(reused);
    }

    @Autowired
    private SecurityAuditService securityAuditService;

    @PostMapping("/audit")
    public SecurityAuditResponse getAuditReport(@RequestBody SecurityAuditRequest request) {
        return securityAuditService.generateAuditReport(request.getPasswords());
    }

    @PostMapping("/weak-passwords")
    public WeakPasswordResponse getWeakPasswords(@RequestBody SecurityAuditRequest request) {

        List<String> weak = securityAuditService.findWeakPasswords(request.getPasswords());

        return new WeakPasswordResponse(weak);
    }

    @Autowired
    private BackupService backupService;

    @PostMapping("/export-backup")
    public BackupResponse exportBackup(@RequestBody SecurityAuditRequest request) {

        String backup = backupService.exportBackup(request.getPasswords());

        return new BackupResponse(backup);
    }
}