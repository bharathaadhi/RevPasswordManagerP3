package com.rev.security.controller;

import com.rev.security.dto.*;
import com.rev.security.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

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
    public SecurityAuditResponse auditPasswords(@RequestBody SecurityAuditRequest request) {

        int total = request.getPasswords().size();

        int weak = securityAuditService.countWeakPasswords(request.getPasswords());

        int reused = securityAuditService.countReusedPasswords(request.getPasswords());

        return new SecurityAuditResponse(total, weak, reused);
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