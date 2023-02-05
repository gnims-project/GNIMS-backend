package com.gnims.project.domain.user.repository;

import com.gnims.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<User> findByUsername(String Username);
    Optional<User> findByKakaoId(Long kakaoId);
}
