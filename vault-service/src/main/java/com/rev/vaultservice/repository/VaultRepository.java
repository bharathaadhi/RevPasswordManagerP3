package com.rev.vaultservice.repository;

import com.rev.vaultservice.entity.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VaultRepository extends JpaRepository<VaultEntry, Long> {
    
    @Query("SELECT v FROM VaultEntry v WHERE v.favorite = true AND v.userId = :userId")
    List<VaultEntry> findByFavoriteTrueAndUserId(@Param("userId") Long userId);
    
    @Query("SELECT v FROM VaultEntry v WHERE v.userId = :userId")
    List<VaultEntry> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VaultEntry v WHERE v.userId = :userId AND v.platform = :platform AND v.username = :username")
    boolean existsByUserIdAndPlatformAndUsername(@Param("userId") Long userId, @Param("platform") String platform, @Param("username") String username);
    
    @Query("SELECT v FROM VaultEntry v WHERE v.platform LIKE %:platform% AND v.userId = :userId")
    List<VaultEntry> findByPlatformContainingAndUserId(@Param("platform") String platform, @Param("userId") Long userId);
    
    @Query("SELECT v FROM VaultEntry v WHERE v.category = :category AND v.userId = :userId")
    List<VaultEntry> findByCategoryAndUserId(@Param("category") String category, @Param("userId") Long userId);
    
    @Query("SELECT v FROM VaultEntry v WHERE v.userId = :userId ORDER BY v.platform ASC")
    List<VaultEntry> findAllByUserIdOrderByPlatformAsc(@Param("userId") Long userId);

    // Keep legacy for now if needed, but we should migrate
    List<VaultEntry> findByFavoriteTrue();
    List<VaultEntry> findByPlatformContaining(String platform);
    List<VaultEntry> findByCategory(String category);
    List<VaultEntry> findAllByOrderByPlatformAsc();
}