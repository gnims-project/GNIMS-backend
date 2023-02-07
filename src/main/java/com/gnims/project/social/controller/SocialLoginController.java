package com.gnims.project.social.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gnims.project.domain.user.dto.LoginResponseDto;
import com.gnims.project.social.dto.SocialCodeDto;
import com.gnims.project.social.sevice.KakaoService;
import com.gnims.project.social.sevice.NaverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/login")
public class SocialLoginController {
    private final KakaoService kakaoService;
    private final NaverService naverService;

    @PostMapping ("/kakao")
    public LoginResponseDto kakaoLogin(@RequestBody SocialCodeDto codeDto, HttpServletResponse response, ModelAndView mv) throws JsonProcessingException {

        // code: 카카오 서버로부터 받은 인가 코드
        return kakaoService.kakaoLogin(codeDto.getCode(), response);
    }

    @GetMapping("/naver")
    public LoginResponseDto naverLogin(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws JsonProcessingException {

        // code: 네이버 서버로부터 받은 인가 코드
        return naverService.naverLogin(code, state, response);
    }
}
