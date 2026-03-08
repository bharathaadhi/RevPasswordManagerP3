package com.rev.vaultservice.repository;

import com.rev.vaultservice.entity.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaultRepository extends JpaRepository<VaultEntry, Long> {
    List<VaultEntry> findByFavoriteTrue();
    List<VaultEntry> findByUserId(Long userId);
    boolean existsByUserIdAndPlatform(Long userId, String platform);
    List<VaultEntry> findByPlatformContaining(String platform);
    List<VaultEntry> findByCategory(String category);
    List<VaultEntry> findAllByOrderByPlatformAsc();
}