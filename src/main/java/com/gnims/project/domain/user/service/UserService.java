package com.gnims.project.domain.user.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.gnims.project.domain.user.dto.*;
import com.gnims.project.domain.user.entity.SocialCode;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.security.jwt.JwtUtil;
import com.gnims.project.share.gmail.EmailService;
import com.gnims.project.social.dto.SocialSignupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.gnims.project.share.message.ExceptionMessage.*;
import static com.gnims.project.share.message.ResponseMessage.CHECK_EMAIL_MESSAGE;
import static com.gnims.project.share.message.ResponseMessage.CHECK_NICKNAME_MESSAGE;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${cloud.aws.s3.bucket}")
    private String S3Bucket;

    @Value("${profile.image}")
    private String defaultImage;

    private final AmazonS3Client amazonS3Client;

    /**
     * Gnims.Auth. + email 을 하는 이유
     * 네이버 소셜 회원가입 하는 사람의 이메일이
     * 기존 이메일 회원가입이 되어 있는 경우에
     * 이메일의 중복이 생길 수 있기 때문에
     * 중복을 차단하기 위해 사용
     * */

    @Transactional
    public void signup(SignupRequestDto request, MultipartFile image) throws IOException {

        String nickname = request.getNickname();

        String email = SocialCode.EMAIL.getValue() + request.getEmail();

        //이메일 / 닉네임 중복체크
        checkDuplicate(email, nickname);

        //비밀번호 암호화
        String password = passwordEncoder.encode(request.getPassword());

        if(image == null) {
            userRepository.save(new User(request.getUsername(), request.getNickname(), email, password, defaultImage));
            return;
        }
        String imageUrl = getImage(image);

        userRepository.save(new User(request.getUsername(), request.getNickname(), email, password, imageUrl));
    }

    private String getImage(MultipartFile image) throws IOException {
        String fileRealName = image.getOriginalFilename();

        //확장자 분리
        String extension = fileRealName.substring(fileRealName.lastIndexOf(".") + 1);

        //허용할 확장자 목록
        List<String> checkFile = new ArrayList<>(List.of(
                "gif", "jfif", "pjpeg", "jpeg",
                "pjp", "jpg", "png", "bmp",
                "dib", "webp", "svgz", "svg"));

        //확장자 체크
        if(!checkFile.contains(extension)) {
            throw new IllegalArgumentException(checkFile + EXTENSION_ERROR_MESSAGE);
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
        return amazonS3Client.getUrl(S3Bucket, originName).toString();
    }

    @Transactional
    public void socialSignup(SocialSignupDto request, MultipartFile image) throws IOException {

        String email = request.getSocialCode().getValue() + request.getEmail();

        //이메일 / 닉네임 중복체크
        checkDuplicate(email, request.getNickname());

        //소셜 회원가입 시 비밀번호 임의 생성
        //비밀번호 암호화
        String password = passwordEncoder.encode(UUID.randomUUID().toString());

        if(image == null) {
            userRepository.save(new User(request.getUsername(), request.getNickname(), email, password, defaultImage));
            return;
        }

        String imageUrl = getImage(image);

        userRepository.save(new User(request.getUsername(), request.getNickname(), email, password, imageUrl));
    }

    private void checkDuplicate(String email, String nickname) {
        //이메일 중복 체크
        if(userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(ALREADY_REGISTERED_EMAIL);
        }

        //닉네임 중복 체크
        if(userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException(DUPLICATE_NICKNAME);
        }
    }

    public SimpleMessageResult checkNickname(NicknameDto request) {

        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            return new SimpleMessageResult(400, DUPLICATE_NICKNAME);
        }
        return new SimpleMessageResult(200, CHECK_NICKNAME_MESSAGE);
    }

    public SimpleMessageResult checkEmail(EmailDto request) {

        //이메일 중복 체크는 일반 회원가입에서만 사용 됨
        if (userRepository.findByEmail(SocialCode.EMAIL.getValue() + request.getEmail()).isPresent()) {
            return new SimpleMessageResult(400, ALREADY_REGISTERED_EMAIL);
        }
        return new SimpleMessageResult(200, CHECK_EMAIL_MESSAGE);
    }

    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {

        User user = userRepository.findByEmail(SocialCode.EMAIL.getValue() + request.getEmail()).orElseThrow(
                () -> new BadCredentialsException(MISMATCH_EMAIL_OR_SECRET)
        );

        //암호화 된 비밀번호를 비교
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(MISMATCH_EMAIL_OR_SECRET);
        }

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(user.getNickname()));

        return new LoginResponseDto(user);
    }

    @Transactional
    public ProfileImageDto updateProfile(MultipartFile image, User user) throws IOException {

        if(image == null) {
            userRepository.findById(user.getId()).get().updateProfile(defaultImage);
            return new ProfileImageDto(defaultImage);
        }

        String imageUrl = getImage(image);

        userRepository.findById(user.getId()).get().updateProfile(imageUrl);

        return new ProfileImageDto(imageUrl);
    }

    public List<SearchAllQueryDto> search(String username, User user, PageRequest pageRequest) {

        return userRepository.userSearch("%" + username + "%",user.getId(), pageRequest);
    }

    @Transactional
    public void authPassword(AuthEmailDto request) throws Exception {

        User user = userRepository.findByEmail(SocialCode.EMAIL.getValue() + request.getEmail()).orElseThrow(
                () -> new IllegalArgumentException(NON_EXISTED_EMAIL)
        );

        emailService.createEmailValidation(user.getNickname(), request.getEmail());
    }

    @Transactional
    public void updatePassword(PasswordDto request, User user) {

        if(request.getOldPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException(THE_SAME_SECRET_AS_BEFORE);
        }

        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException(CURRENT_MISMATCHED_SECRET);
        }

        String newPassword = passwordEncoder.encode(request.getNewPassword());

        userRepository.findByNickname(user.getNickname()).get().updatePassword(newPassword);
    }
}
