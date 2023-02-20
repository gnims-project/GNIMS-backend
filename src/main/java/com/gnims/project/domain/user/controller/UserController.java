package com.gnims.project.domain.user.controller;

import com.gnims.project.domain.schedule.dto.ReadScheduleResult;
import com.gnims.project.domain.user.NicknameEmailDto;
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
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/auth/signup")
    public ResponseEntity<SimpleMessageResult> signup(
            @Validated(ValidationSequence.class) @RequestPart(value = "data") SignupRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        userService.signup(request, image);
        return new ResponseEntity<>(new SimpleMessageResult(CREATED.value(), "회원가입 완료"), CREATED);
    }

    @PostMapping("/social/signup")
    public ResponseEntity<SimpleMessageResult> socialSignup(
            @Validated(ValidationSequence.class) @RequestPart(value = "data") SocialSignupDto request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        userService.socialSignup(request, image);
        return new ResponseEntity<>(new SimpleMessageResult(CREATED.value(), "회원가입 완료"), CREATED);
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
    public ResponseEntity<SimpleMessageResult> updateProfile(@RequestPart(value = "image", required = false) MultipartFile image,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        userService.updateProfile(image, userDetails.getUser());
        return new ResponseEntity<>(new SimpleMessageResult(OK.value(), "프로필 변경 성공"), OK);
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
    public ResponseEntity<ReadScheduleResult> search(@RequestParam(value = "username") String username,
                                                     @RequestParam(value = "page") Integer page,
                                                     @RequestParam(value = "size") Integer size,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PageRequest pageRequest = PageRequest.of(page, size);
        List<SearchResponseDto> response = userService.search(username, pageRequest, userDetails.getUser());

        return new ResponseEntity<>(new ReadScheduleResult<>(OK.value(), "유저 검색 성공", response), OK);
    }

    //이메일 인증을 날릴 api
    @GetMapping("/auth/password")
    public void authPassword(@RequestBody NicknameEmailDto request) throws Exception {

        userService.authPassword(request);
    }

    //이메일 인증 x 비밀번호 재설정
    @PatchMapping("/users/password")
    public ResponseEntity<SimpleMessageResult> updatePassword(@Validated(ValidationSequence.class) @RequestBody PasswordDto request, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        userService.updatePassword(request, userDetails.getUser());

        return new ResponseEntity<>(new SimpleMessageResult(OK.value(), "비밀번호 바꾸기 성공"), OK);
    }
}














