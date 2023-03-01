package com.gnims.project.domain.user.entity;

import lombok.Getter;

@Getter
public enum SocialCode {
    /**
     * 변수명 및 값의 길이를 안 맞출 시
     * 로그인시의 리턴 값 이메일이 이상하게 잘려 갈 수 있습니다.
     * */
    EMAIL("Gnims.Email."), KAKAO("Gnims.Kakao."), NAVER("Gnims.Naver.");

    private String value;

    SocialCode(String value) {
        this.value = value;
    }
}
