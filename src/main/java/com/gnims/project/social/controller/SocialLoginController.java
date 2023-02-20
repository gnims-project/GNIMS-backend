package com.gnims.project.social.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gnims.project.social.dto.SocialCodeDto;
import com.gnims.project.social.dto.SocialResult;
import com.gnims.project.social.dto.SocialTokenDto;
import com.gnims.project.social.sevice.KakaoService;
import com.gnims.project.social.sevice.NaverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping ("/kakao/login")
    public ResponseEntity<SocialResult> kakaoLogin(@RequestBody SocialCodeDto codeDto, HttpServletResponse response) throws JsonProcessingException {

        // code: 카카오 서버로부터 받은 인가 코드
        SocialResult result = kakaoService.kakaoLogin(codeDto.getCode(), response);
        return new ResponseEntity<>(result, HttpStatus.valueOf(result.getStatus()));
    }

    @PostMapping ("/naver/login")
    public ResponseEntity<SocialResult> naverLogin(@RequestBody SocialTokenDto tokenDto, HttpServletResponse response) throws JsonProcessingException {

//        System.out.println("________________________________________token: " + request.getHeader("token"));
        System.out.println("________________________________________token: " + tokenDto.getToken());

        // 프론트에서 naver token 을 받아옴
        SocialResult result = naverService.naverLogin(tokenDto.getToken(), response);
        return new ResponseEntity<>(result, HttpStatus.valueOf(result.getStatus()));
    }
}
