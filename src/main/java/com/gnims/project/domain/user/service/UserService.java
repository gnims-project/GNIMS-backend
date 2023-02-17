package com.gnims.project.domain.user.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.gnims.project.domain.friendship.dto.PageDto;
import com.gnims.project.domain.friendship.dto.PagingDataResponse;
import com.gnims.project.domain.friendship.entity.FollowStatus;
import com.gnims.project.domain.friendship.entity.Friendship;
import com.gnims.project.domain.friendship.repository.FriendshipRepository;
import com.gnims.project.domain.user.NicknameEmailDto;
import com.gnims.project.domain.user.dto.*;
import com.gnims.project.domain.user.entity.SocialCode;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.security.jwt.JwtUtil;
import com.gnims.project.social.dto.SocialSignupDto;
import com.gnims.project.util.gmail.EmailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final EmailServiceImpl emailServiceImpl;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final List<String> CHO = List.of("ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ");

    @Value("${cloud.aws.s3.bucket}")
    private String S3Bucket;

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

        String email = SocialCode.AUTH.getValue() + request.getEmail();

        //이메일 / 닉네임 중복체크
        checkDuplicate(email, nickname);

        String search = request.getUsername() + "@" + request.getNickname();

        char[] chars = search.toCharArray();

        String searchNickname = "";

        for(char char1: chars) {
            searchNickname += char1;
            if('가' <= char1 && char1 <= '힣') {
                searchNickname = searchNickname + CHO.get((char1-'가')/28/21);
            }
        }

        //비밀번호 암호화
        String password = passwordEncoder.encode(request.getPassword());

        if(image == null) {
            userRepository.save(new User(request.getUsername(), nickname, searchNickname, email, password, "https://gnims99.s3.ap-northeast-2.amazonaws.com/ProfilImg.png"));
            return;
        }
        String imageUrl = getImage(image);

        userRepository.save(new User(request.getUsername(), nickname, searchNickname, email, password, imageUrl));
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
            throw new IllegalArgumentException(checkFile + " 확장자의 이미지 파일만 업로드 가능합니다!");
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

        String nickname = request.getNickname();

        char[] chars = nickname.toCharArray();

        String searchNickname = "";

        for(char char1: chars) {
            searchNickname += char1;
            if('가' <= char1 && char1 <= '힣') {
                searchNickname = searchNickname + CHO.get((char1-'가')/28/21);
            }
        }

        //소셜 회원가입 시 비밀번호 임의 생성
        //비밀번호 암호화
        String password = passwordEncoder.encode(UUID.randomUUID().toString());

        if(image == null) {
            userRepository.save(new User(request.getUsername(), nickname, searchNickname, email, password, "https://gnims99.s3.ap-northeast-2.amazonaws.com/ProfilImg.png"));
            return;
        }

        String imageUrl = getImage(image);

        userRepository.save(new User(request.getUsername(), nickname, searchNickname, email, password, imageUrl));
    }

    private void checkDuplicate(String email, String nickname) {
        //이메일 중복 체크
        if(userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일 입니다");
        }

        //닉네임 중복 체크
        if(userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("중복된 닉네임 입니다");
        }
    }

    public SimpleMessageResult checkNickname(NicknameDto request) {

        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            return new SimpleMessageResult(400, "중복된 닉네임 입니다.");
        }
        return new SimpleMessageResult(200, "사용 가능한 닉네임 입니다.");
    }

    public SimpleMessageResult checkEmail(EmailDto request) {

        //이메일 중복 체크는 일반 회원가입에서만 사용 됨
        if (userRepository.findByEmail(SocialCode.AUTH.getValue() + request.getEmail()).isPresent()) {
            return new SimpleMessageResult(400, "이미 등록된 이메일 입니다.");
        }
        return new SimpleMessageResult(200, "사용 가능한 이메일 입니다.");
    }

    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {

        User user = userRepository.findByEmail(SocialCode.AUTH.getValue() + request.getEmail()).orElseThrow(
                () -> new BadCredentialsException("이메일 혹은 비밀번호가 일치하지 않습니다.")
        );

        //암호화 된 비밀번호를 비교
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("이메일 혹은 비밀번호가 일치하지 않습니다.");
        }

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(user.getNickname()));

        return new LoginResponseDto(user.getNickname(), request.getEmail(), user.getProfileImage());
    }

    @Transactional
    public void updateProfile(MultipartFile image, User user) throws IOException {

        if(image == null) {
            userRepository.findById(user.getId()).get().updateProfile("https://gnims99.s3.ap-northeast-2.amazonaws.com/ProfilImg.png");
            return;
        }

        String imageUrl = getImage(image);

        userRepository.findById(user.getId())
                .get().updateProfile(imageUrl);
    }

    /*
    * 검색 기존
    * */

//    public SearchResponseDto search(String nickname, User user) {
//
//        //검색한 닉네임을 찾음
//        User searchedUser = userRepository.findByNickname(nickname).orElseThrow(
//                () -> new IllegalArgumentException("해당 닉네임의 유저가 존재하지 않습니다.")
//        );
//
//        Optional<Friendship> friend = friendshipRepository
//                .findAllByMyself_IdAndFollowing_Id(user.getId(), searchedUser.getId());
//
//        //팔로우 한 적이 없거나, 언 팔로우 상태 일 경우
//        if(friend.isEmpty() || FollowStatus.INACTIVE.equals(friend.get().getStatus())) {
//            return new SearchResponseDto(searchedUser, false);
//        }
//
//        return new SearchResponseDto(searchedUser, true);
//    }

    public boolean check(User searchedUser, User user) {
        Optional<Friendship> friend = friendshipRepository
                .findAllByMyself_IdAndFollowing_Id(user.getId(), searchedUser.getId());

        return friend.isEmpty() || FollowStatus.INACTIVE.equals(friend.get().getStatus());
    }

    /*
    * 검색 업그레이드 v2
    * */
    //본인 제외

    //현재 정렬 ID 내림차순
    public PagingDataResponse search(String nickname, PageRequest pageRequest, User user) {

        System.out.println("nickname2 = " + user.getNickname());

        if(nickname == null || "".equals(nickname) || nickname.length()>8) {
            throw new IllegalArgumentException("8자리 이하의 닉네임을 검색해 주세요!");
        }

        nickname = nickname.trim();

        //공백 제거
        String nickname2 = nickname.replace(" ", "");

        char[] chars = nickname2.toCharArray();

        nickname2 = "@?";

        for(char char1: chars) {
            nickname2 = nickname2 + "[a-zA-Z0-9ㄱ-ㅎ가-힣]*";
            String checkChar = "" + char1;
            if(checkChar.matches("^[가-힣]$")) {
                nickname2 = nickname2 + char1;
                nickname2 = nickname2 + CHO.get((char1-'가')/28/21);
                continue;
            }
            nickname2 += char1;
        };

        nickname2.replaceFirst("[a-zA-Z0-9ㄱ-ㅎ가-힣]*", "");

        System.out.println("searchNickname = " + nickname2);

        Page<User> users = userRepository.searchByRegExpKeyword(nickname2 + "@?", pageRequest);
        PageDto page = new PageDto(users);

//        List<SearchResponseDto> data = users.stream()
//                .map(u -> new SearchResponseDto(u, !check(u, user)))
//                .collect(Collectors.toList());

        List<SearchResponseDto> data = new ArrayList<>();

        for(User u: users) {
            if (u.getId().equals(user.getId())) {
                continue;
            }

            data.add(new SearchResponseDto(u, !check(u, user)));
        }


        return new PagingDataResponse(page, data);
    }

    /*
    * 검색 업그레이드 v1
    * */

    //    public List<String> testSearch(String nickname) {
//
//        nickname = nickname.trim();
//
//        String searchNickname= nickname.replace(' ','|');
//
//        //특문 안받는 다는 가정
//
//        return userRepository.searchByRegExpKeyword(searchNickname).get();
//    }

    @Transactional
    public void authPassword(NicknameEmailDto request) throws Exception {

        Optional<User> user = userRepository.findByNickname(request.getNickname());

        if(user.isEmpty() || !(request.getEmail()).equals(user.get().getEmail())) {
            throw new IllegalArgumentException("닉네임 혹은 이메일이 일치하지 않습니다.");
        }

        emailServiceImpl.sendSimpleMessage(request.getNickname(), request.getEmail());
    }

    @Transactional
    public void updatePassword(PasswordDto request, User user) {

        if(request.getOldPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("기존의 비밀번호와 같은 비밀번호 입니다!");
        }

        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String newPassword = passwordEncoder.encode(request.getNewPassword());

        userRepository.findByNickname(user.getNickname()).get().updatePassword(newPassword);
    }
}
