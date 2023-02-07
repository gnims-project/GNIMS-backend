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
    @Enumerated(value = EnumType.STRING)
    private SocialCode socialCode;

    @Column
    private String socialId;

    public User(SignupRequestDto request) {
        this.username = request.getNickname();
        this.email = request.getEmail();
        this.password = request.getPassword();
    }

    public User(String username, SocialCode socialCode, String socialId, String email, String password) {
        this.username = username;
        this.socialCode = socialCode;
        this.socialId = socialId;
        this.email = email;
        this.password = password;
    }

    public User socialIdUpdate(SocialCode socialCode, String socialId) {
        this.socialCode = socialCode;
        this.socialId = socialId;
        return this;
    }
}
