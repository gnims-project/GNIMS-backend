package com.gnims.project.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gnims.project.domain.user.dto.LoginRequestDto;
import com.gnims.project.domain.user.dto.LoginResponseDto;
import com.gnims.project.domain.user.dto.MessageResponseDto;
import com.gnims.project.domain.user.dto.SignupRequestDto;
import com.gnims.project.domain.user.kakao.KakaoService;
import com.gnims.project.domain.user.naver.NaverService;
import com.gnims.project.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final KakaoService kakaoService;
    private final NaverService naverService;

    @PostMapping("/auth/signup")
    public MessageResponseDto signup(@RequestBody SignupRequestDto request) {

        return userService.signup(request);
    }

    @PostMapping("/auth/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request, HttpServletResponse response) {

        return userService.login(request, response);
    }

    @GetMapping("/api/user/kakao/callback")
    public LoginResponseDto kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {

        // code: 카카오 서버로부터 받은 인가 코드
        return kakaoService.kakaoLogin(code, response);
    }

    @GetMapping("/api/user/naver/callback")
    public LoginResponseDto naverLogin(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws JsonProcessingException {

        // code: 네이버 서버로부터 받은 인가 코드
        return naverService.naverLogin(code, state, response);
    }
}
