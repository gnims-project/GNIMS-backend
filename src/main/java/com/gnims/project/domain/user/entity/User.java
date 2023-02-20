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

//    @Column(nullable = false)
//    private String searchNickname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String profileImage;

//    @Column(nullable = false)
//    @Enumerated(value = EnumType.STRING)
//    private SocialCode socialCode;
//
//    @Column
//    private String socialId;

    public User(String username, String nickname,/* String searchNickname,*/ String email, String password, String imageUrl) {
        this.username = username;
        this.nickname = nickname;
//        this.searchNickname = searchNickname;
        this.email = email;
        this.password = password;
        this.profileImage = imageUrl;
    }

//    public String getNickname() {
//        return nickname.replaceAll("[ㄱ-ㅎ]", "");
//    }


    public String getEmail() {
        return email.substring(12);
    }

    public void updateProfile(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    //    public User(String username, String nickname, SocialCode socialCode, /*String socialId, */String email, String password) {
//        this.username = username;
//        this.nickname = nickname;
////        this.socialId = socialId;
//        this.email = email;
//        this.password = password;
//        this.socialCode = socialCode;
//    }



    /*
    * 기존 유저와 소셜 유저 강제 통합 x
    * */

//    public User socialIdUpdate(SocialCode socialCode, String socialId) {
//        this.socialCode = socialCode;
//        this.socialId = socialId;
//        return this;
//    }
}
