package com.gnims.project.domain.user.controller;

import com.gnims.project.domain.friendship.dto.PagingDataResponse;
import com.gnims.project.domain.user.dto.*;
import com.gnims.project.domain.user.service.UserService;
import com.gnims.project.security.service.UserDetailsImpl;
import com.gnims.project.social.dto.SocialSignupDto;
import com.gnims.project.util.validation.ValidationSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/auth/signup")
    public MessageResponseDto signup(@Validated(ValidationSequence.class) @RequestBody SignupRequestDto request) {

        return userService.signup(request);
    }

    @PostMapping("/social/signup")
    public MessageResponseDto signup(@Validated(ValidationSequence.class) @RequestBody SocialSignupDto request) {

        return userService.socialSignup(request);
    }

    @PostMapping("/auth/nickname")
    public MessageResponseDto checkNickname(@Validated(ValidationSequence.class) @RequestBody NicknameDto request) {

        return userService.checkNickname(request);
    }

    @PostMapping("/auth/email")
    public MessageResponseDto checkEmail(@Validated(ValidationSequence.class) @RequestBody EmailDto request) {

        return userService.checkEmail(request);
    }

    @PatchMapping("/users/profile")
    public MessageResponseDto updateProfile(@RequestPart(value = "file") MultipartFile image,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        return userService.updateProfile(image, userDetails.getUser());
    }

    @PostMapping("/auth/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request, HttpServletResponse response) {

        return userService.login(request, response);
    }

//    @GetMapping("/users/search")
//    public SearchResponseDto search(@RequestParam(value = "nickname") String nickname,
//                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
//
//        return userService.search(nickname, userDetails.getUser());
//    }

//    @GetMapping("/test/search")
//    public List<String> testsearch(@RequestParam(value = "nickname") String nickname/*,
//                                   @AuthenticationPrincipal UserDetailsImpl userDetails*/) {
//
//        return userService.testSearch(nickname/*, userDetails.getUser()*/);
//    }

    @GetMapping("/users/search")
    public PagingDataResponse testsearch2(@RequestParam(value = "nickname") String nickname,
                                          @RequestParam Integer number,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PageRequest pageRequest = PageRequest.of(number, 5);
        return userService.testSearch2(nickname, pageRequest, userDetails.getUser());
    }
}
