package com.gnims.project.util.gmail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailValidation, Long> {
//    Optional<EmailValidation> findByCode(String code);
    Optional<EmailValidation> findByEmail(String email);
    void deleteByEmail(String email);
}
