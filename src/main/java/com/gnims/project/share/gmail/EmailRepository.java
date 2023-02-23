package com.gnims.project.share.gmail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailValidation, Long> {
//    Optional<EmailValidation> findByCode(String code);
    Optional<EmailValidation> findByEmail(String email);

    @Transactional
    void deleteByCreateAtBefore(LocalDateTime time);
}
