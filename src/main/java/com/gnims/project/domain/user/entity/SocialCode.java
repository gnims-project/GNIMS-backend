package com.gnims.project.domain.user.entity;

import lombok.Getter;

@Getter
public enum SocialCode {
    AUTH("Gnims.Auth."), KAKAO("Gnims.Kakao."), NAVER("Gnims.Naver.");

    private String value;

    SocialCode(String value) {
        this.value = value;
    }
}
