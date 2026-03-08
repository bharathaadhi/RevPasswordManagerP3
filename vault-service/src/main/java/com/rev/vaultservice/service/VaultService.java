package com.rev.vaultservice.service;

import com.rev.vaultservice.dto.VaultRequestDto;
import com.rev.vaultservice.entity.VaultEntry;

import java.util.List;

public interface VaultService {

    VaultEntry saveVault(VaultRequestDto dto);
    List<VaultEntry> getFavorites();
    List<VaultEntry> getAllVaults();
    VaultEntry updateVault(Long id, VaultRequestDto dto);
    List<VaultEntry> getByUser(Long userId);
    void deleteVaultWithCode(Long id, String code);
    void deleteVault(Long id);
    String revealPassword(Long id, String masterPassword);
    List<VaultEntry> searchByPlatform(String platform);
    String revealPassword(Long id);
    String generateCode();
    VaultEntry markFavorite(Long id);
    List<VaultEntry> getByCategory(String category);
    List<VaultEntry> sortByPlatform();
}