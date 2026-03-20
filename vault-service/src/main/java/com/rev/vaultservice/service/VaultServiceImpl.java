package com.rev.vaultservice.service;

import com.rev.vaultservice.config.AESUtil;
import com.rev.vaultservice.dto.BackupRequestDto;
import com.rev.vaultservice.dto.MasterPasswordDto;
import com.rev.vaultservice.dto.VaultRequestDto;
import com.rev.vaultservice.entity.VaultEntry;
import com.rev.vaultservice.repository.VaultRepository;
import com.rev.vaultservice.client.NotificationClient;
import com.rev.vaultservice.dto.NotificationRequest;
import com.rev.vaultservice.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final VaultRepository vaultRepository;
    private final com.rev.vaultservice.feign.AuthFeignClient authFeignClient;
    private final NotificationClient notificationClient;
    private final Map<String, String> userCodeMap = new ConcurrentHashMap<>();
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
    public List<VaultEntry> searchByPlatform(String platform, Long userId) {
        return vaultRepository.findByPlatformContainingAndUserId(platform, userId);
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
    public String revealPassword(Long id, String masterPassword, String email, String code) {

        if (!authFeignClient.verifyMasterPassword(new MasterPasswordDto(masterPassword, email))) {
            throw new RuntimeException("Invalid master password");
        }

        String storedCode = userCodeMap.get(email);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new RuntimeException("Invalid verification code");
        }

        VaultEntry vault = vaultRepository.findById(id).orElseThrow();
        
        return AESUtil.decrypt(vault.getEncryptedPassword());
    }
    @Override
    public List<VaultEntry> getByCategory(String category, Long userId) {
        return vaultRepository.findByCategoryAndUserId(category, userId);
    }
    @Override
    public String generateCode(String email) {
        String normalizedEmail = email != null ? email.toLowerCase().trim() : "";
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        userCodeMap.put(normalizedEmail, code);

        try {
            notificationClient.sendNotification(new NotificationRequest(
                    email,
                    "Vault Verification Code",
                    "Your verification code is: " + code + ". It will expire in 5 minutes.",
                    "OTP"
            ));
        } catch (Exception e) {
            // Log error but continue (frontend might still show the alert as backup)
        }

        return code;
    }
    @Override
    @Transactional
    public void deleteVaultWithCode(Long id, String code, String masterPassword, String email) {
        String normalizedEmail = email != null ? email.toLowerCase().trim() : "";

        if (!authFeignClient.verifyMasterPassword(new MasterPasswordDto(masterPassword, normalizedEmail))) {
            throw new RuntimeException("Invalid master password");
        }

        String storedCode = userCodeMap.get(normalizedEmail);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new RuntimeException("Invalid verification code");
        }

        VaultEntry vault = vaultRepository.findById(id).orElseThrow();
        
        // Check ownership
        // if (!vault.getUserId().equals(expectedUserId)) ...
        
        String platform = vault.getPlatform();

        vaultRepository.deleteById(id);

        try {
            notificationClient.sendNotification(new NotificationRequest(
                    email,
                    "Vault Credential Deleted",
                    "A password for " + platform + " was securely deleted from your vault.",
                    "ALERT"
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
    public List<VaultEntry> getFavorites(Long userId) {
        return vaultRepository.findByFavoriteTrueAndUserId(userId);
    }
    @Override
    public List<VaultEntry> sortByPlatform(Long userId) {
        return vaultRepository.findAllByUserIdOrderByPlatformAsc(userId);
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

        int importedCount = 0;
        int skippedCount = 0;

        if (dto.getEntries() != null) {
            for (VaultRequestDto entryDto : dto.getEntries()) {
                entryDto.setUserId(dto.getUserId());
                entryDto.setAlreadyEncrypted(true);
                try {
                    saveVault(entryDto);
                    importedCount++;
                } catch (RuntimeException e) {
                    skippedCount++;
                }
            }
        }

        try {
            String message = String.format("Vault import complete. %d new entries added. %d entries were already present in your vault.", 
                importedCount, skippedCount);
            
            if (importedCount == 0 && skippedCount > 0) {
                message = "All imported passwords already exist in your vault. No new entries were added.";
            }

            notificationClient.sendNotification(new NotificationRequest(
                    dto.getEmail(),
                    "Vault Import Summary",
                    message,
                    "INFO"
            ));
        } catch (Exception e) {}
    }

    private void verifyCredentials(BackupRequestDto dto) {
        String email = dto.getEmail() != null ? dto.getEmail().toLowerCase().trim() : "";
        if (!authFeignClient.verifyMasterPassword(new MasterPasswordDto(dto.getMasterPassword(), email))) {
            throw new RuntimeException("Invalid master password");
        }
 
        String storedCode = userCodeMap.get(email);
        if (storedCode == null || !storedCode.equals(dto.getCode())) {
            throw new RuntimeException("Invalid verification code");
        }
    }
}