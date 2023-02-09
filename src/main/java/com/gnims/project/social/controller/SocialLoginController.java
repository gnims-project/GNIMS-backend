package com.gnims.project.social.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gnims.project.social.dto.SocialCodeDto;
import com.gnims.project.social.dto.SocialLoginDto;
import com.gnims.project.social.sevice.KakaoService;
import com.gnims.project.social.sevice.NaverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class SocialLoginController {
    private final KakaoService kakaoService;
    private final NaverService naverService;

    @PostMapping ("/kakao/login")
    public SocialLoginDto kakaoLogin(@RequestBody SocialCodeDto codeDto, HttpServletResponse response) throws JsonProcessingException {

        // code: 카카오 서버로부터 받은 인가 코드
        return kakaoService.kakaoLogin(codeDto.getCode(), response);
    }

    @GetMapping  ("/naver/login")
    public SocialLoginDto naverLogin(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {

        // 프론트에서 naver token 을 받아옴
        return naverService.naverLogin(request.getHeader("token"), response);
    }
}
