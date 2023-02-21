package com.gnims.project.domain.friendship.controller;

import com.gnims.project.domain.friendship.dto.*;
import com.gnims.project.domain.friendship.service.FriendshipService;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.gnims.project.domain.friendship.entity.FollowStatus.INIT;
import static com.gnims.project.util.ResponseMessage.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;


@RestController
@RequiredArgsConstructor
@Transactional
public class FriendshipController {

    private final FriendshipService friendshipService;

    // 팔로잉 조회(내가 등록한 친구)
    @GetMapping("/friendship/followings")
    public ResponseEntity<FriendshipResult> readFollowing(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long myId = userDetails.receiveUserId();
        List<FollowReadResponse> followings = friendshipService.readFollowing(myId);

        return new ResponseEntity<>(new FriendshipResult<>(OK.value(), READ_FOLLOWINGS_MESSAGE, followings), OK);
    }
    // 팔로잉 조회 - 페이징 처리
    @GetMapping("/v2-page/friendship/followings")
    public ResponseEntity<FriendshipResult> readFollowingPage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                      @RequestParam Integer page,
                                                                      @RequestParam Integer size) {
        Long myselfId = userDetails.receiveUserId();
        PageRequest pageRequest = PageRequest.of(page, size);
        List<FollowReadResponse> followings = friendshipService.readFollowingPage(myselfId, pageRequest);

        return new ResponseEntity<>(new FriendshipResult<>(OK.value(), READ_FOLLOWINGS_MESSAGE, followings), OK);
    }

    //팔로워 조회(나를 등록한 친구)
    @GetMapping("/proto/friendship/followers")
    public ResponseEntity<FriendshipResult> readFollowerProto(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long myselfId = userDetails.receiveUserId();
        List<FollowReadResponse> followers = friendshipService.readFollowerProto(myselfId);

        return new ResponseEntity<>(new FriendshipResult(OK.value(), "팔로워 조회 완료", followers), OK);
    }

    //팔로워 조회(나를 등록한 친구) - 테스트 코드 작성 후 proto 타입 지울 예정
    @GetMapping("/friendship/followers")
    public ResponseEntity<FriendshipResult> readFollower(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long myselfId = userDetails.receiveUserId();
        List<FollowReadResponse> followers = friendshipService.readFollower(myselfId);

        return new ResponseEntity<>(new FriendshipResult(OK.value(), READ_FOLLOWERS_MESSAGE, followers), OK);
    }

    // 팔로워 조회 - 페이징 처리
    @GetMapping("/v2-page/friendship/followers")
    public ResponseEntity<FriendshipResult> readFollowerPage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                             @RequestParam Integer page,
                                                             @RequestParam Integer size) {
        Long myselfId = userDetails.receiveUserId();
        PageRequest pageRequest = PageRequest.of(page, size);
        List<FollowReadResponse> followers = friendshipService.readFollowerPage(myselfId, pageRequest);

        return new ResponseEntity<>(new FriendshipResult<>(OK.value(), READ_FOLLOWERS_MESSAGE, followers), OK);
    }

    // 팔로잉 하기/취소
    @PostMapping("/friendship/followings/{followings-id}")
    public ResponseEntity<FriendshipResult> presFollowButton(@PathVariable("followings-id") Long followingId,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long myselfId = userDetails.receiveUserId();
        FriendshipResponse response = friendshipService.makeFriendship(myselfId, followingId);

        if (response.getStatus().equals(INIT)) {
            return new ResponseEntity<>(new FriendshipResult<>(CREATED.value(), response.receiveStatusMessage(), response), CREATED);
        }

        return new ResponseEntity<>(new FriendshipResult<>(OK.value(), response.receiveStatusMessage(), response), OK);

    }

    //팔로잉 수
    @GetMapping("/friendship/followings/counting")
    public ResponseEntity<FriendshipResult> countFollowing(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long myselfId = userDetails.receiveUserId();
        Integer count = friendshipService.countFollowing(myselfId);

        return new ResponseEntity<>(new FriendshipResult(OK.value(), COUNT_FOLLOWINGS_MESSAGE, count),OK);
    }

    //팔로워 수
    @GetMapping("/friendship/followers/counting")
    public ResponseEntity<FriendshipResult> countFollower(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long myselfId = userDetails.receiveUserId();
        Integer count = friendshipService.countFollower(myselfId);

        return new ResponseEntity<>(new FriendshipResult(OK.value(), COUNT_FOLLOWERS_MESSAGE, count),OK);
    }
}
