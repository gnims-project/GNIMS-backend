package com.gnims.project.social.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gnims.project.social.dto.SocialCodeDto;
import com.gnims.project.social.dto.SocialResult;
import com.gnims.project.social.dto.SocialTokenDto;
import com.gnims.project.social.sevice.KakaoService;
import com.gnims.project.social.sevice.NaverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class SocialLoginController {
    private final KakaoService kakaoService;
    private final NaverService naverService;

    @PostMapping ("/social/kakao-login")
    public ResponseEntity<SocialResult> kakaoLogin(@RequestBody SocialCodeDto codeDto,
                                                   HttpServletResponse response) throws JsonProcessingException {

        // code: 카카오 서버로부터 받은 인가 코드
        SocialResult result = kakaoService.kakaoLogin(codeDto.getCode(), response);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping ("/social/naver-login")
    public ResponseEntity<SocialResult> naverLogin(@RequestBody SocialTokenDto tokenDto,
                                                   HttpServletResponse response) throws JsonProcessingException {

        // token: 네이버 서버로부터 받은 사용자 정보 접근 토큰
        SocialResult result = naverService.naverLogin(tokenDto.getToken(), response);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
