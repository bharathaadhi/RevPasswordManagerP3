package com.rev.vaultservice.service;

import com.rev.vaultservice.config.AESUtil;
import com.rev.vaultservice.dto.VaultRequestDto;
import com.rev.vaultservice.entity.VaultEntry;
import com.rev.vaultservice.repository.VaultRepository;
import com.rev.vaultservice.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final VaultRepository vaultRepository;
    private String generatedCode;
    @Override
    public VaultEntry saveVault(VaultRequestDto dto) {

        if (vaultRepository.existsByUserIdAndPlatform(dto.getUserId(), dto.getPlatform())) {
            throw new RuntimeException("Credential already exists for this platform");
        }

        VaultEntry vault = new VaultEntry();
        vault.setUserId(dto.getUserId());
        vault.setPlatform(dto.getPlatform());
        vault.setUsername(dto.getUsername());
        vault.setEncryptedPassword(AESUtil.encrypt(dto.getEncryptedPassword()));
        vault.setCategory(dto.getCategory());
        vault.setFavorite(dto.isFavorite());

        return vaultRepository.save(vault);
    }

    @Override
    public List<VaultEntry> getAllVaults() {
        return vaultRepository.findAll();
    }

    @Override
    public List<VaultEntry> getByUser(Long userId) {
        return vaultRepository.findByUserId(userId);
    }

    @Override
    public void deleteVault(Long id) {
        vaultRepository.deleteById(id);
    }

    @Override
    public List<VaultEntry> searchByPlatform(String platform) {
        return vaultRepository.findByPlatformContaining(platform);
    }

    @Override
    public VaultEntry markFavorite(Long id) {
        VaultEntry vault = vaultRepository.findById(id).orElseThrow();
        vault.setFavorite(true);
        return vaultRepository.save(vault);
    }
    @Override
    public String revealPassword(Long id) {
        VaultEntry vault = vaultRepository.findById(id).orElseThrow();
        return AESUtil.decrypt(vault.getEncryptedPassword());
    }
    @Override
    public String revealPassword(Long id, String masterPassword) {

        if (!masterPassword.equals("admin123")) {
            throw new RuntimeException("Invalid master password");
        }

        VaultEntry vault = vaultRepository.findById(id).orElseThrow();

        return AESUtil.decrypt(vault.getEncryptedPassword());
    }
    @Override
    public List<VaultEntry> getByCategory(String category) {
        return vaultRepository.findByCategory(category);
    }
    @Override
    public String generateCode() {
        generatedCode = String.valueOf((int)(Math.random() * 900000) + 100000);
        return generatedCode;
    }
    @Override
    public void deleteVaultWithCode(Long id, String code) {

        if (!code.equals(generatedCode)) {
            throw new RuntimeException("Invalid verification code");
        }

        vaultRepository.deleteById(id);
    }
    @Override
    public VaultEntry updateVault(Long id, VaultRequestDto dto) {

        VaultEntry vault = vaultRepository.findById(id).orElseThrow();

        vault.setPlatform(dto.getPlatform());
        vault.setUsername(dto.getUsername());
        vault.setEncryptedPassword(AESUtil.encrypt(dto.getEncryptedPassword()));
        vault.setCategory(dto.getCategory());
        vault.setFavorite(dto.isFavorite());

        return vaultRepository.save(vault);
    }
    @Override
    public List<VaultEntry> getFavorites() {
        return vaultRepository.findByFavoriteTrue();
    }
    @Override
    public List<VaultEntry> sortByPlatform() {
        return vaultRepository.findAllByOrderByPlatformAsc();
    }
}