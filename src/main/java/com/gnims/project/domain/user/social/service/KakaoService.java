package com.gnims.project.domain.user.social.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnims.project.domain.user.dto.MessageResponseDto;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.domain.user.social.dto.SocialProfileDto;
import com.gnims.project.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${kakao.tokenUri}")
    private String kakaoTokenUri;

    @Value("${kakao.clientId}")
    private String kakaoClientId;

    @Value("${kakao.redirectUri}")
    private String kakaoCallbackUri;

    @Value("${kakao.userInfoUri}")
    private String kakaoProfileUri;

    public ResponseEntity<?> login(String code) throws JsonProcessingException {

        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        SocialProfileDto profile = getKakaoUserInfo(accessToken);

        // 3. 필요시에 회원가입
        User kakaoUser = saveUser(profile);

        // 4. JWT 토큰 반환
        String token = jwtUtil.createToken(kakaoUser.getUsername());

        // 5. 헤더에 토큰을 담기
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(JwtUtil.AUTHORIZATION_HEADER, token);

        // 토큰 던져주기
        return ResponseEntity.ok().headers(httpHeaders).body(
                new MessageResponseDto("카카오 성공! 헤더를 보세용 -> Authorization"));
    }

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getToken(String code) throws JsonProcessingException {

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoCallbackUri);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                kakaoTokenUri,
                HttpMethod.POST,
                kakaoTokenRequest,
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
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                kakaoProfileUri,
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
        return new SocialProfileDto(id, nickname, email);
    }

    // 3. 필요시에 회원가입
    private User saveUser(SocialProfileDto profile) {

        // DB 에 중복된 Kakao Id 가 있는지 확인
        Optional<User> kakaoUser = userRepository.findByKakaoId(profile.getId());
        if (kakaoUser.isEmpty()) {

            if(userRepository.existsByEmail(profile.getEmail())) {
                throw new IllegalArgumentException("카카오 이메일과 동일한 이메일로 가입한 회원이 존재합니다.");
            }

            User user = new User(profile.getNickname(),
                    profile.getId(),
                    profile.getEmail(),
                    passwordEncoder.encode(UUID.randomUUID().toString()));

            userRepository.save(user);

            return user;
        }
        return kakaoUser.get();
    }
}