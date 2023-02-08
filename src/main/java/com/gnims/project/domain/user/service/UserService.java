package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.dto.LoginRequestDto;
import com.gnims.project.domain.user.dto.LoginResponseDto;
import com.gnims.project.domain.user.dto.MessageResponseDto;
import com.gnims.project.domain.user.dto.SignupRequestDto;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public MessageResponseDto signup(SignupRequestDto request, Errors errors) {

        if (errors.hasErrors()) {
            return getError(errors);
        }

        //이메일 중복 체크
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일 입니다");
        }

        //닉네임 중복 체크
        if(userRepository.findByUsername(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("중복된 닉네임 입니다");
        }

        userRepository.save(new User(request));

        return new MessageResponseDto("회원가입 성공!");
    }

    public MessageResponseDto getError(Errors errors) {

        /* 유효성 통과 못한 필드와 메시지를 핸들링 */
        Map<String, String> validatorResult = new HashMap<>();

        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = String.format("%s", error.getField());
            validatorResult.put(validKeyName, error.getDefaultMessage());
        }

        /* 회원가입 실패 메시지 */
        return new MessageResponseDto(validatorResult.toString());
    }

    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {

        String email = request.getEmail();
        String password = request.getPassword();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BadCredentialsException("등록된 사용자가 없습니다.")
        );

        if(!user.getPassword().equals(password)) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(user.getUsername()));

        return new LoginResponseDto(user.getUsername(), user.getEmail());
    }
}
