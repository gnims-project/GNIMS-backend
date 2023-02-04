package com.gnims.project.domain.user.controller;

import com.gnims.project.domain.user.dto.LoginRequestDto;
import com.gnims.project.domain.user.dto.MessageResponseDto;
import com.gnims.project.domain.user.dto.SignupRequestDto;
import com.gnims.project.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/auth/signup")
    public MessageResponseDto signup(@RequestBody SignupRequestDto request) {

        return userService.signup(request);
    }

    @PostMapping("/auth/login")
    public MessageResponseDto login(@RequestBody LoginRequestDto request, HttpServletResponse response) {

        return userService.login(request, response);
    }
}
