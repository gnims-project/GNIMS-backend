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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MessageResponseDto signup(SignupRequestDto request) {

        //이메일 중복 체크
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일 입니다");
        }

        //닉네임 중복 체크
        if(userRepository.findByUsername(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("중복된 닉네임 입니다");
        }

        //비밀번호 암호화
        String password = passwordEncoder.encode(request.getPassword());

        userRepository.save(new User(request.getNickname(), request.getEmail(), password));

        return new MessageResponseDto("회원가입 성공!");
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
