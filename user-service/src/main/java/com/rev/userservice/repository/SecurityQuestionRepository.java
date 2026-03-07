package com.rev.userservice.repository;

import com.rev.userservice.entity.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {

    Optional<SecurityQuestion> findByEmail(String email);
}