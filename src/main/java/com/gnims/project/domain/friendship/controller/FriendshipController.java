package com.gnims.project.domain.friendship.controller;

import com.gnims.project.domain.friendship.dto.FollowingResponse;
import com.gnims.project.domain.friendship.dto.FriendshipResult;
import com.gnims.project.domain.friendship.service.FriendshipService;
import com.gnims.project.security.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;


@RestController
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    // 팔로잉 조회(내가 등록한 친구)
    @GetMapping("/friendship/followings")
    public ResponseEntity<FriendshipResult> readFollowing(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long myId = userDetails.getUser().getId();
        List<FollowingResponse> followings = friendshipService.readFollowing(myId);

        return new ResponseEntity<>(new FriendshipResult<>(OK.value(), "팔로잉 조회 완료", followings), OK);
    }
}
