package com.gnims.project.domain.user.service;

import com.gnims.project.domain.user.dto.MessageResponseDto;
import com.gnims.project.domain.user.dto.SignupRequestDto;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository memberRepository;

    @Transactional
    public MessageResponseDto signup(SignupRequestDto request) {

        //이메일 중복 체크
        if(memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일 입니다");
        }

        //닉네임 중복 체크
        if(memberRepository.findByUsername(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("중복된 닉네임 입니다");
        }

        memberRepository.save(new User(request));

        return new MessageResponseDto("회원가입 성공!");
    }
}
