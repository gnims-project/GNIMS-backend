package com.gnims.project.domain.user.naver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.kakao.SocialProfileDto;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NaverService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${naver.tokenUri}")
    private String naverTokenUri;

    @Value("${naver.clientId}")
    private String naverClientId;

    @Value("${naver.redirectUri}")
    private String naverCallbackUri;

    @Value("${naver.clientSecret}")
    private String naverClientSecret;

    @Value("${naver.userInfoUri}")
    private String naverProfileUri;

    public String naverLogin(String code, String state) throws JsonProcessingException {

        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code, state);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        SocialProfileDto naverUserInfo = getKakaoUserInfo(accessToken);

        // 3. 필요시에 회원가입
        User naverUser = registerNaverUserIfNeeded(naverUserInfo);

        // 4. JWT 토큰 반환
        String createToken =  jwtUtil.createToken(naverUser.getUsername());
        // 토큰 던져주기
        return createToken;
    }

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getToken(String code, String state) throws JsonProcessingException {

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(naverTokenUri)
                .queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", naverCallbackUri)
                .queryParam("client_id", naverClientId)
                .queryParam("code", code)
                .queryParam("state", state)
                .queryParam("client_secret", naverClientSecret)
                .build();

        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.postForEntity(
                builder.toString(),
                null,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
    private SocialProfileDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.postForEntity(
                naverProfileUri,
                naverUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        System.out.println("jsonNode = " + jsonNode.toString());
        String id = jsonNode.get("response").get("id").asText();
        String nickname = jsonNode.get("response")
                .get("nickname").asText();
        String email = jsonNode.get("response")
                .get("email").asText();

        log.info("네이버 사용자 정보: " + id + ", " + nickname + ", " + email);
        return new SocialProfileDto(id, nickname, email);
    }

    // 3. 필요시에 회원가입
    private User registerNaverUserIfNeeded(SocialProfileDto naverUserInfo) {

        // DB 에 중복된 naver Id 가 있는지 확인
        String naverId = naverUserInfo.getId();
        User naverUser = userRepository.findBySocialId(naverId)
                .orElse(null);
        if (naverUser == null) {
            String naverEmail = naverUserInfo.getEmail();
            naverUser = getKakaoUser(naverUserInfo, naverId, naverEmail);
            userRepository.flush();
        }
        return naverUser;
    }

    //DB에서 사용자의 카카오 정보를 추가, 없을 시 DB에 저장 후 가져옴
    @Transactional
    User getKakaoUser(SocialProfileDto naverUserInfo, String naverId, String naverEmail) {

        //카카오 이메일과 동일한 이메일 유저가 있는경우
        if (userRepository.findByEmail(naverEmail).isPresent()) {
            return userRepository
                    .findByEmail(naverEmail).get()
                    // 기존 회원정보에 카카오 Id 추가
                    .socialIdUpdate("NAVER", naverId);
        }

        // 신규 회원가입
        // password: random UUID
        String password = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(password);

        // email: kakao email
        String email = naverUserInfo.getEmail();

        User naverUser = new User(naverUserInfo.getNickname(), "NAVER", naverId, email, encodedPassword);

        userRepository.save(naverUser);
        return naverUser;
    }
}
