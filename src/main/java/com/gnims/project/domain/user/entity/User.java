package com.gnims.project.domain.user.entity;

import com.gnims.project.domain.user.dto.SignupRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private Long kakaoId;

    public User(SignupRequestDto request) {
        this.username = request.getNickname();
        this.email = request.getEmail();
        this.password = request.getPassword();
    }

    public User(String username, Long kakaoId, String email, String password) {
        this.username = username;
        this.kakaoId = kakaoId;
        this.email = email;
        this.password = password;
    }

    public User kakaoIdUpdate(Long kakaoId) {
        this.kakaoId = kakaoId;
        return this;
    }
}
