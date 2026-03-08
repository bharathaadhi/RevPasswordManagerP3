package com.rev.userservice.repository;

import com.rev.userservice.entity.TwoFactorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Long> {
    Optional<TwoFactorCode> findByEmail(String email);
}