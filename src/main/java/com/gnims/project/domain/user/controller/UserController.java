package com.gnims.project.domain.user.controller;

import com.gnims.project.domain.user.dto.*;
import com.gnims.project.domain.user.service.UserService;
import com.gnims.project.security.service.UserDetailsImpl;
import com.gnims.project.share.validation.ValidationSequence;
import com.gnims.project.social.dto.SocialSignupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.gnims.project.share.message.ResponseMessage.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/signup")
    public ResponseEntity<SimpleMessageResult> signup(
            @Validated(ValidationSequence.class) @RequestPart(value = "data") SignupRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        userService.signup(request, image);
        return new ResponseEntity<>(new SimpleMessageResult(CREATED.value(), SIGNUP_SUCCESS_MESSAGE), CREATED);
    }

    @PostMapping("/social/signup")
    public ResponseEntity<SimpleMessageResult> socialSignup(
            @Validated(ValidationSequence.class) @RequestPart(value = "data") SocialSignupDto request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        userService.socialSignup(request, image);
        return new ResponseEntity<>(new SimpleMessageResult(CREATED.value(), SIGNUP_SUCCESS_MESSAGE), CREATED);
    }

    @PostMapping("/auth/nickname")
    public ResponseEntity<SimpleMessageResult> checkNickname(@Validated(ValidationSequence.class)
                                                                 @RequestBody NicknameDto request) {
        SimpleMessageResult result = userService.checkNickname(request);
        return status(result.getStatus()).body(result);
    }

    @PostMapping("/auth/email")
    public ResponseEntity<SimpleMessageResult> checkEmail(@Validated(ValidationSequence.class)
                                                              @RequestBody EmailDto request) {
        SimpleMessageResult result = userService.checkEmail(request);
        return status(result.getStatus()).body(result);
    }

    @PatchMapping("/users/profile")
    public ResponseEntity<UserResult> updateProfile(@RequestPart(value = "image", required = false) MultipartFile image,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        ProfileImageDto result = userService.updateProfile(image, userDetails.getUser());
        return ok (new UserResult<>(OK.value(), PROFILE_UPDATE_SUCCESS_MESSAGE, result));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserResult> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        LoginResponseDto result = userService.login(request, response);
        return ok (new UserResult<>(OK.value(), LOGIN_SUCCESS_MESSAGE, result));
    }

    //이메일 인증 x 비밀번호 재설정
    @PatchMapping("/users/password")
    public ResponseEntity<SimpleMessageResult> updatePassword(@Validated(ValidationSequence.class)
                                                                  @RequestBody PasswordDto request,
                                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.updatePassword(request, userDetails.getUser());
        return ok (new SimpleMessageResult(OK.value(), SECRET_UPDATE_SUCCESS_MESSAGE));
    }

    //최적화 검색
    @GetMapping("/users/search")
    public ResponseEntity<UserResult> testSearch(@RequestParam(value = "username") String username,
                                                         @RequestParam(value = "page") Integer page,
                                                         @RequestParam(value = "size") Integer size,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ok (new UserResult<>(OK.value(), USER_SEARCH_SUCCESS_MESSAGE,
                        userService.search(username, userDetails.getUser(), pageRequest)));
    }
}














