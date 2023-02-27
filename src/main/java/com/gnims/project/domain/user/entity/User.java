package com.gnims.project.domain.user.entity;

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
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String profileImage;

    public User(String username, String nickname, String email, String password, String imageUrl) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.profileImage = imageUrl;
    }

    public String makePureEmail() {
        //여기서 12는 DB의 이메일 앞에 붙은 "Gnims.~~"를 제거 하기 위한 숫자입니다.
        return email.substring(12);
    }

    public void updateProfile(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
