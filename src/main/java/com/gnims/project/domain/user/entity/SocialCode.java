package com.gnims.project.domain.user.entity;

import lombok.Getter;

@Getter
public enum SocialCode {
    AUTH("Auth"), KAKAO("Kakao"), NAVER("Naver");

    private String value;

    SocialCode(String value) {
        this.value = value;
    }
}
