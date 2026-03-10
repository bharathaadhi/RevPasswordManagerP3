package com.rev.userservice.repository;

import com.rev.userservice.entity.TwoFactorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Long> {
    Optional<TwoFactorCode> findByEmail(String email);
    Optional<TwoFactorCode> findFirstByEmailOrderByIdDesc(String email);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM TwoFactorCode t WHERE t.email = :email")
    void deleteByEmail(@org.springframework.data.repository.query.Param("email") String email);
}