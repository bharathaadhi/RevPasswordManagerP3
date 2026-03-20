package com.rev.vaultservice.service;

import com.rev.vaultservice.dto.BackupRequestDto;
import com.rev.vaultservice.dto.VaultRequestDto;
import com.rev.vaultservice.entity.VaultEntry;

import java.util.List;

public interface VaultService {

    VaultEntry saveVault(VaultRequestDto dto);
    List<VaultEntry> getAllVaults();
    List<VaultEntry> getByUser(Long userId);
    void deleteVault(Long id);
    List<VaultEntry> searchByPlatform(String platform, Long userId);
    VaultEntry markFavorite(Long id, boolean favorite);
    String revealPassword(Long id);
    String revealPassword(Long id, String masterPassword, String email, String code);
    List<VaultEntry> getByCategory(String category, Long userId);
    String generateCode(String email);
    void deleteVaultWithCode(Long id, String code, String masterPassword, String email);
    VaultEntry updateVault(Long id, VaultRequestDto dto);
    List<VaultEntry> getFavorites(Long userId);
    List<VaultEntry> sortByPlatform(Long userId);
    List<VaultEntry> exportVaultSecure(BackupRequestDto dto);
    void importVaultSecure(BackupRequestDto dto);
}