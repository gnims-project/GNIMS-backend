package com.gnims.project.security.service;

import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final List<String> CHO = List.of("ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ");

    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {

        char[] chars = nickname.toCharArray();

        String searchNickname = "";

        for(char char1: chars) {
            searchNickname += char1;
            if('가' <= char1 && char1 <= '힣') {
                searchNickname = searchNickname + CHO.get((char1-'가')/28/21);
            }
        }

        User user = userRepository.findByNickname(searchNickname)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return new UserDetailsImpl(user, user.getNickname());
    }

}