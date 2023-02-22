package com.gnims.project.share.gmail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailValidation, Long> {
//    Optional<EmailValidation> findByCode(String code);
    Optional<EmailValidation> findByEmail(String email);
    void deleteByCreateAtBefore(LocalDateTime time);
}
