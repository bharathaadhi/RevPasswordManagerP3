package com.rev.vaultservice.controller;

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
    public VaultEntry save(@RequestBody VaultRequestDto dto) {
        return vaultService.saveVault(dto);
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
        return vaultService.revealPassword(id, dto.getMasterPassword());
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
    public String secureDelete(@PathVariable Long id, @RequestBody VerificationCodeDto dto) {
        vaultService.deleteVaultWithCode(id, dto.getCode());
        return "Deleted Successfully";
    }
    @PutMapping("/update/{id}")
    public VaultEntry update(@PathVariable Long id, @RequestBody VaultRequestDto dto) {
        return vaultService.updateVault(id, dto);
    }
    @GetMapping("/favorites")
    public List<VaultEntry> favorites() {
        return vaultService.getFavorites();
    }
    @GetMapping("/sort")
    public List<VaultEntry> sort() {
        return vaultService.sortByPlatform();
    }
}