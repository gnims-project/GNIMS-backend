package com.gnims.project.domain.user.controller;

import com.gnims.project.domain.friendship.dto.PagingDataResponse;
import com.gnims.project.domain.user.dto.*;
import com.gnims.project.domain.user.service.UserService;
import com.gnims.project.security.service.UserDetailsImpl;
import com.gnims.project.social.dto.SocialSignupDto;
import com.gnims.project.util.validation.ValidationSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/auth/signup")
    public ResponseEntity<SimpleMessageResult> signup(@Validated(ValidationSequence.class)
                                                          @RequestBody SignupRequestDto request) {

        userService.signup(request);
        return new ResponseEntity<>(new SimpleMessageResult(CREATED.value(), "회원가입 완료"), OK);
    }

    @PostMapping("/social/signup")
    public ResponseEntity<SimpleMessageResult> socialSignup(@Validated(ValidationSequence.class)
                                                                @RequestBody SocialSignupDto request) {

        userService.socialSignup(request);
        return new ResponseEntity<>(new SimpleMessageResult(CREATED.value(), "회원가입 완료"), OK);
    }

    @PostMapping("/auth/nickname")
    public ResponseEntity<SimpleMessageResult> checkNickname(@Validated(ValidationSequence.class)
                                                                 @RequestBody NicknameDto request) {

        SimpleMessageResult result = userService.checkNickname(request);
        return new ResponseEntity<>(result, HttpStatus.valueOf(result.getStatus()));
    }

    @PostMapping("/auth/email")
    public ResponseEntity<SimpleMessageResult> checkEmail(@Validated(ValidationSequence.class)
                                                              @RequestBody EmailDto request) {

        SimpleMessageResult result = userService.checkEmail(request);
        return new ResponseEntity<>(result, HttpStatus.valueOf(result.getStatus()));
    }

    @PatchMapping("/users/profile")
    public ResponseEntity<SimpleMessageResult> updateProfile(@RequestPart(value = "file") MultipartFile image,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        userService.updateProfile(image, userDetails.getUser());
        return new ResponseEntity<>(new SimpleMessageResult(CREATED.value(), "프로필 변경 성공"), OK);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserResult> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {

        LoginResponseDto result = userService.login(request, response);
        return new ResponseEntity<>(new UserResult<>(OK.value(), "로그인 성공", result), OK);
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
    public ResponseEntity<SearchPageableResult> search(@RequestParam(value = "nickname") String nickname,
                                          @RequestParam Integer number,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PageRequest pageRequest = PageRequest.of(number, 5);
        PagingDataResponse response = userService.search(nickname, pageRequest, userDetails.getUser());

        return new ResponseEntity<>(new SearchPageableResult<>(OK.value(), "유저 검색 성공", response), OK);
    }
}














