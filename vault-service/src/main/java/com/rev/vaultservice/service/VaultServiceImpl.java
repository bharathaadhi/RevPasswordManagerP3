package com.rev.vaultservice.service;

import com.rev.vaultservice.config.AESUtil;
import com.rev.vaultservice.dto.BackupRequestDto;
import com.rev.vaultservice.dto.MasterPasswordDto;
import com.rev.vaultservice.dto.VaultRequestDto;
import com.rev.vaultservice.entity.VaultEntry;
import com.rev.vaultservice.repository.VaultRepository;
import com.rev.vaultservice.service.VaultService;
import com.rev.vaultservice.client.NotificationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final VaultRepository vaultRepository;
    private final com.rev.vaultservice.feign.AuthFeignClient authFeignClient;
    private final NotificationClient notificationClient;
    private String generatedCode;
    @Override
    @Transactional
    public VaultEntry saveVault(VaultRequestDto dto) {

        if (vaultRepository.existsByUserIdAndPlatformAndUsername(dto.getUserId(), dto.getPlatform(), dto.getUsername())) {
            throw new RuntimeException("Credential already exists for this platform and username");
        }

        VaultEntry vault = new VaultEntry();
        vault.setUserId(dto.getUserId());
        vault.setPlatform(dto.getPlatform());
        vault.setUsername(dto.getUsername());

        // FIX: Check if already encrypted (imported)
        if (dto.isAlreadyEncrypted()) {
            vault.setEncryptedPassword(dto.getEncryptedPassword());
        } else {
            vault.setEncryptedPassword(AESUtil.encrypt(dto.getEncryptedPassword()));
        }

        vault.setCategory(dto.getCategory());
        vault.setFavorite(dto.isFavorite());
        vault.setStrength(dto.getStrength());

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
    @Transactional
    public void deleteVault(Long id) {
        vaultRepository.deleteById(id);
    }

    @Override
    public List<VaultEntry> searchByPlatform(String platform) {
        return vaultRepository.findByPlatformContaining(platform);
    }

    @Override
    @Transactional
    public VaultEntry markFavorite(Long id, boolean favorite) {
        VaultEntry vault = vaultRepository.findById(id).orElseThrow();
        vault.setFavorite(favorite);
        return vaultRepository.save(vault);
    }
    @Override
    public String revealPassword(Long id) {
        VaultEntry vault = vaultRepository.findById(id).orElseThrow();
        return AESUtil.decrypt(vault.getEncryptedPassword());
    }
    @Override
    public String revealPassword(Long id, String masterPassword, String email) {

        if (!authFeignClient.verifyMasterPassword(new MasterPasswordDto(masterPassword, email))) {
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
    @Transactional
    public void deleteVaultWithCode(Long id, String code, String masterPassword, String email) {

        if (!authFeignClient.verifyMasterPassword(new MasterPasswordDto(masterPassword, email))) {
            throw new RuntimeException("Invalid master password");
        }

        if (!code.equals(generatedCode)) {
            throw new RuntimeException("Invalid verification code");
        }

        VaultEntry vault = vaultRepository.findById(id).orElseThrow();
        String platform = vault.getPlatform();

        vaultRepository.deleteById(id);

        try {
            notificationClient.sendNotification(Map.of(
                    "recipientEmail", email,
                    "title", "Vault Credential Deleted",
                    "message", "A password for " + platform + " was securely deleted from your vault.",
                    "type", "ALERT"
            ));
        } catch (Exception e) {}
    }
    @Override
    @Transactional
    public VaultEntry updateVault(Long id, VaultRequestDto dto) {

        VaultEntry vault = vaultRepository.findById(id).orElseThrow();

        vault.setPlatform(dto.getPlatform());
        vault.setUsername(dto.getUsername());
        vault.setEncryptedPassword(AESUtil.encrypt(dto.getEncryptedPassword()));
        vault.setCategory(dto.getCategory());
        vault.setFavorite(dto.isFavorite());
        vault.setStrength(dto.getStrength());

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
    @Override
    public List<VaultEntry> exportVaultSecure(BackupRequestDto dto) {
        verifyCredentials(dto);
        return getByUser(dto.getUserId());
    }

    @Override
    @Transactional
    public void importVaultSecure(BackupRequestDto dto) {
        verifyCredentials(dto);

        if (dto.getEntries() != null) {
            for (VaultRequestDto entryDto : dto.getEntries()) {
                // Ensure the imported entry belongs to the current user
                entryDto.setUserId(dto.getUserId());
                // FIX: Flag that this is already encrypted from the backup file
                entryDto.setAlreadyEncrypted(true);
                try {
                    saveVault(entryDto);
                } catch (RuntimeException e) {
                    // Skip duplicates during import
                }
            }
        }

        try {
            notificationClient.sendNotification(Map.of(
                    "recipientEmail", dto.getEmail(),
                    "title", "Vault Import Completed",
                    "message", "A new set of passwords was securely imported into your vault.",
                    "type", "INFO"
            ));
        } catch (Exception e) {}
    }

    private void verifyCredentials(BackupRequestDto dto) {
        if (!authFeignClient.verifyMasterPassword(new MasterPasswordDto(dto.getMasterPassword(), dto.getEmail()))) {
            throw new RuntimeException("Invalid master password");
        }

        if (generatedCode == null || !generatedCode.equals(dto.getCode())) {
            throw new RuntimeException("Invalid verification code");
        }
    }
}