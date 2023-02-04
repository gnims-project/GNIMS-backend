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

    public User(SignupRequestDto request) {
        this.username = request.getNickname();
        this.email = request.getEmail();
        this.password = request.getPassword();
    }
}
