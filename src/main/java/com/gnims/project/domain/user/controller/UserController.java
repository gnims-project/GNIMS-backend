package com.gnims.project.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gnims.project.domain.user.dto.LoginRequestDto;
import com.gnims.project.domain.user.dto.MessageResponseDto;
import com.gnims.project.domain.user.dto.SignupRequestDto;
import com.gnims.project.domain.user.kakao.KakaoService;
import com.gnims.project.domain.user.service.UserService;
import com.gnims.project.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final KakaoService kakaoService;

    @PostMapping("/auth/signup")
    public MessageResponseDto signup(@RequestBody SignupRequestDto request) {

        return userService.signup(request);
    }

    @PostMapping("/auth/login")
    public MessageResponseDto login(@RequestBody LoginRequestDto request, HttpServletResponse response) {

        return userService.login(request, response);
    }

    @GetMapping("/auth/kakao-login")
    public void kakaoLoginPage(HttpServletResponse response) throws IOException {

        response.sendRedirect("https://kauth.kakao.com/oauth/authorize?client_id=6e659b5f78ef7ca493658b8cefa98aa2&redirect_uri=https://eb.jxxhxxx.shop/api/user/kakao/callback&response_type=code");
    }

    @GetMapping("/api/user/kakao/callback")
    public MessageResponseDto kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {

        // code: 카카오 서버로부터 받은 인가 코드
        String createToken = kakaoService.kakaoLogin(code);

        // Cookie 생성 및 직접 브라우저에 Set
        Cookie cookie = new Cookie(JwtUtil.AUTHORIZATION_HEADER, createToken.substring(7));
        cookie.setPath("/");
        response.addCookie(cookie);

        return new MessageResponseDto("성공했나요? 쿠키를 보세용 -> Authorization");
    }
}
