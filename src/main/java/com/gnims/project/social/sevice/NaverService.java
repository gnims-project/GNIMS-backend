package com.gnims.project.social.sevice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.security.jwt.JwtUtil;
import com.gnims.project.social.dto.SocialLoginDto;
import com.gnims.project.social.dto.SocialProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class NaverService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /*
    * 사용 안하지만 혹시 몰라서 남겨둠
    * */

//    @Value("${naver.tokenUri}")
//    private String naverTokenUri;
//    @Value("${naver.clientId}")
//    private String naverClientId;
//    @Value("${naver.redirectUri}")
//    private String naverCallbackUri;
//    @Value("${naver.clientSecret}")
//    private String naverClientSecret;

    @Value("${naver.userInfoUri}")
    private String naverProfileUri;

    public SocialLoginDto naverLogin(String token, HttpServletResponse response) throws JsonProcessingException {

        // 1. 토큰으로 네이버 API 호출 : "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
        SocialProfileDto naverUserInfo = getNaverUserInfo(token);

//        // 2. 필요시에 회원가입
//        User naverUser = registerNaverUserIfNeeded(naverUserInfo);
//
//        // 3. JWT 토큰 담기
//        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(naverUser.getUsername()));

        // DB 에 중복된 Naver email 이 있는지 확인
        String naverEmail = "Gnims.Naver." + naverUserInfo.getEmail();
        User naverUser = userRepository.findByEmail(naverEmail)
                .orElse(null);
        if (naverUser == null) {
            return new SocialLoginDto("non-member", "", naverUserInfo.getEmail());
        }

        // 4. JWT 토큰 담기
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(naverUser.getNickname()));

        return new SocialLoginDto("member", naverUser.getNickname(), naverUserInfo.getEmail());
    }

    // 2. 토큰으로 네이버 API 호출 : "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
    private SocialProfileDto getNaverUserInfo(String accessToken) throws JsonProcessingException {

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        // Header 에 naver 에 넘길 access 토큰 담기
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();

        //naver 서버에 요청해서 유저 정보를 받아옴
        ResponseEntity<String> response = rt.postForEntity(
                naverProfileUri,
                naverUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        //네이버 식별자 id
        String id = jsonNode.get("response").get("id").asText();
        //네이버 유저 이름
        String nickname = jsonNode.get("response").get("name").asText();
        //네이버 유저 이메일
        String email = jsonNode.get("response").get("email").asText();

        log.info("네이버 사용자 정보: " + id + ", " + nickname + ", " + email);

        return new SocialProfileDto(email);
    }

//    // 3. 필요시에 회원가입
//    private User registerNaverUserIfNeeded(SocialProfileDto naverUserInfo) {
//
//        // DB 에 중복된 naver Id 가 있는지 확인
//        String naverId = naverUserInfo.getId();
//        User naverUser = userRepository.findBySocialId(naverId)
//                .orElse(null);
//        if (naverUser == null) {
//            String naverEmail = naverUserInfo.getEmail();
//            naverUser = getNaverUser(naverUserInfo, naverId, naverEmail);
//            userRepository.flush();
//        }
//        return naverUser;
//    }
//
//    //DB에서 사용자의 네이버 정보를 추가, 없을 시 DB에 저장 후 가져옴
//    @Transactional
//    User getNaverUser(SocialProfileDto naverUserInfo, String naverId, String naverEmail) {
//
//        //네이버 이메일과 동일한 이메일 유저가 있는경우
//        if (userRepository.findByEmail(naverEmail).isPresent()) {
//            return userRepository
//                    .findByEmail(naverEmail).get()
//                    // 기존 회원정보에 네이버 Id 추가
//                    .socialIdUpdate(SocialCode.NAVER, naverId);
//        }
//
//        // 신규 회원가입
//        // password: random UUID
//        String password = UUID.randomUUID().toString();
//        String encodedPassword = passwordEncoder.encode(password);
//
//        // email: naver email
//        String email = naverUserInfo.getEmail();
//
//        User naverUser = new User(naverUserInfo.getNickname(), SocialCode.NAVER, naverId, email, encodedPassword);
//
//        userRepository.save(naverUser);
//        return naverUser;
//    }
}
