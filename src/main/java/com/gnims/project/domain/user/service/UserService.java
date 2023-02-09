package com.gnims.project.domain.user.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.gnims.project.domain.user.dto.*;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private String pt = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$";


    @Value("${cloud.aws.s3.bucket}")
    private String S3Bucket;

    private final AmazonS3Client amazonS3Client;

    @Transactional
    public MessageResponseDto signup(SignupRequestDto request) {

        String email = "Gnims." + request.getSocialCode().getValue() + "." + request.getEmail();

        //이메일 중복 체크
        if(userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일 입니다");
        }

        //닉네임 중복 체크
        if(userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("중복된 닉네임 입니다");
        }

        String password = request.getPassword();

        //소셜 회원가입
        if (request.getPassword() == null) {
            password = UUID.randomUUID().toString();
        }

        //일반 회원가입
        else if(!Pattern.matches(pt, password)) {
            throw new IllegalArgumentException("영문자와 숫자가 포함된 8 ~ 20 자리의 비밀번호만 가능합니다.");
        }

        //비밀번호 암호화
        password = passwordEncoder.encode(password);

        userRepository.save(new User(request.getUsername(), request.getNickname(), email, password));

        return new MessageResponseDto("회원가입 성공!");
    }

    public MessageResponseDto checkNickname(NicknameDto request) {

        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            return new MessageResponseDto("중복된 닉네임 입니다");
        }
        return new MessageResponseDto("사용 가능한 닉네임 입니다");
    }

    public MessageResponseDto checkEmail(EmailDto request) {

        String email = "Gnims." + request.getSocialCode().getValue() + "." + request.getEmail();

        if (userRepository.findByEmail(email).isPresent()) {
            return new MessageResponseDto("이미 등록된 이메일 입니다");
        }
        return new MessageResponseDto("사용 가능한 이메일 입니다");
    }

    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {

        String email = "Gnims.Auth." + request.getEmail();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BadCredentialsException("등록된 사용자가 없습니다.")
        );

        //암호화 된 비밀번호를 비교
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(user.getNickname()));

        return new LoginResponseDto(user.getNickname(), request.getEmail());
    }

    @Transactional
    public MessageResponseDto updateProfile(MultipartFile image, User user) throws IOException {

        if(image == null || Objects.equals(image.getOriginalFilename(), "")) {
            throw new IllegalArgumentException("이미지를 넣어 주세요!");
        }

        String originName = UUID.randomUUID().toString();
        long size = image.getSize();

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(image.getContentType());
        objectMetadata.setContentLength(size);

        amazonS3Client.putObject(
                new PutObjectRequest(S3Bucket, originName, image.getInputStream(), objectMetadata )
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );
        String imageUrl = amazonS3Client.getUrl(S3Bucket, originName).toString();

        userRepository.findById(user.getId())
                .get().updateProfile(imageUrl);

        return new MessageResponseDto("성공!");
    }
}
