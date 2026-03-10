package com.rev.vaultservice.controller;

import com.rev.vaultservice.dto.BackupRequestDto;
import com.rev.vaultservice.dto.MasterPasswordDto;
import com.rev.vaultservice.dto.VaultRequestDto;
import com.rev.vaultservice.dto.VerificationCodeDto;
import com.rev.vaultservice.entity.VaultEntry;
import com.rev.vaultservice.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;

    @PostMapping("/save")
    public org.springframework.http.ResponseEntity<?> save(@RequestBody VaultRequestDto dto) {
        try {
            VaultEntry entry = vaultService.saveVault(dto);
            return org.springframework.http.ResponseEntity.ok(entry);
        } catch (RuntimeException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public List<VaultEntry> getAll() {
        return vaultService.getAllVaults();
    }

    @GetMapping("/user/{userId}")
    public List<VaultEntry> getByUser(@PathVariable Long userId) {
        return vaultService.getByUser(userId);
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        vaultService.deleteVault(id);
        return "Deleted Successfully";
    }

    @GetMapping("/search")
    public List<VaultEntry> search(@RequestParam String platform) {
        return vaultService.searchByPlatform(platform);
    }

    @PutMapping("/favorite/{id}")
    public VaultEntry favorite(@PathVariable Long id) {
        return vaultService.markFavorite(id);
    }
    @PostMapping("/reveal/{id}")
    public String reveal(@PathVariable Long id, @RequestBody MasterPasswordDto dto) {
        return vaultService.revealPassword(id, dto.getMasterPassword(), dto.getEmail());
    }
    @GetMapping("/category/{category}")
    public List<VaultEntry> getByCategory(@PathVariable String category) {
        return vaultService.getByCategory(category);
    }
    @GetMapping("/generate-code")
    public String generateCode() {
        return vaultService.generateCode();
    }
    @PostMapping("/delete-secure/{id}")
    public org.springframework.http.ResponseEntity<?> secureDelete(@PathVariable Long id, @RequestBody VerificationCodeDto dto) {
        try {
            vaultService.deleteVaultWithCode(id, dto.getCode(), dto.getMasterPassword(), dto.getEmail());
            return org.springframework.http.ResponseEntity.ok("Deleted Successfully");
        } catch (RuntimeException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/update/{id}")
    public org.springframework.http.ResponseEntity<?> update(@PathVariable Long id, @RequestBody VaultRequestDto dto) {
        try {
            VaultEntry entry = vaultService.updateVault(id, dto);
            return org.springframework.http.ResponseEntity.ok(entry);
        } catch (RuntimeException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/favorites")
    public List<VaultEntry> favorites() {
        return vaultService.getFavorites();
    }
    @GetMapping("/sort")
    public List<VaultEntry> sort() {
        return vaultService.sortByPlatform();
    }

    @PostMapping("/export-secure")
    public List<VaultEntry> exportSecure(@RequestBody BackupRequestDto dto) {
        return vaultService.exportVaultSecure(dto);
    }

    @PostMapping("/import-secure")
    public String importSecure(@RequestBody BackupRequestDto dto) {
        vaultService.importVaultSecure(dto);
        return "Imported Successfully";
    }
}