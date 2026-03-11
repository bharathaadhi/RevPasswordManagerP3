package com.rev.vaultservice.service;

import com.rev.vaultservice.dto.BackupRequestDto;
import com.rev.vaultservice.dto.VaultRequestDto;
import com.rev.vaultservice.entity.VaultEntry;

import java.util.List;

public interface VaultService {

    VaultEntry saveVault(VaultRequestDto dto);
    List<VaultEntry> getFavorites();
    List<VaultEntry> getAllVaults();
    VaultEntry updateVault(Long id, VaultRequestDto dto);
    List<VaultEntry> getByUser(Long userId);
    void deleteVaultWithCode(Long id, String code, String masterPassword, String email);
    void deleteVault(Long id);
    String revealPassword(Long id, String masterPassword, String email);
    List<VaultEntry> searchByPlatform(String platform);
    String revealPassword(Long id);
    String generateCode();
    VaultEntry markFavorite(Long id, boolean favorite);
    List<VaultEntry> getByCategory(String category);
    List<VaultEntry> sortByPlatform();

    List<VaultEntry> exportVaultSecure(BackupRequestDto dto);
    void importVaultSecure(BackupRequestDto dto);
}