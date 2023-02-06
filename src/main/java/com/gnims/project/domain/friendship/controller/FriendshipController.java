package com.gnims.project.domain.friendship.controller;

import com.gnims.project.domain.friendship.dto.FollowResponse;
import com.gnims.project.domain.friendship.dto.FollowingResponse;
import com.gnims.project.domain.friendship.dto.FriendshipResult;
import com.gnims.project.domain.friendship.service.FriendshipService;
import com.gnims.project.security.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static com.gnims.project.domain.friendship.entity.FollowStatus.*;
import static org.springframework.http.HttpStatus.CREATED;
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

    // 팔로잉 하기/취소
    @PostMapping("/friendship/followings/{followings-id}")
    public ResponseEntity<FriendshipResult> presFollowButton(@PathVariable("followings-id") Long userId,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long myId = userDetails.getUser().getId();
        FollowResponse response = friendshipService.clickFollowButton(myId, userId);

        if (response.getStatus().equals(INIT)) {
            return new ResponseEntity<>(new FriendshipResult<>(CREATED.value(), response.receiveFollowStatus(), response), CREATED);
        }

        return new ResponseEntity<>(new FriendshipResult<>(OK.value(), response.receiveFollowStatus(), response), OK);

    }
}
