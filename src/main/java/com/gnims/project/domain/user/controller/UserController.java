package com.gnims.project.domain.user.controller;

import com.gnims.project.domain.user.dto.LoginRequestDto;
import com.gnims.project.domain.user.dto.LoginResponseDto;
import com.gnims.project.domain.user.dto.MessageResponseDto;
import com.gnims.project.domain.user.dto.SignupRequestDto;
import com.gnims.project.domain.user.service.UserService;
import com.gnims.project.util.validation.ValidationSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/auth/signup")
    public MessageResponseDto signup(@Validated(ValidationSequence.class) @RequestBody SignupRequestDto request, Errors errors) {

        return userService.signup(request, errors);
    }

    @PostMapping("/auth/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request, HttpServletResponse response) {

        return userService.login(request, response);
    }
}
