package com.gnims.project.domain.user.social.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gnims.project.domain.user.social.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class SocialLoginController {
     private final KakaoService kakaoService;

     @GetMapping("/kakao")
     public ResponseEntity<?> getKakaoLogin(@RequestParam String code) throws JsonProcessingException {
          return kakaoService.login(code);
     }
}
