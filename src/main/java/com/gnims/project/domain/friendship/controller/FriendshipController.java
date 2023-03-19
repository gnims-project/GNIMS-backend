package com.gnims.project.domain.friendship.controller;

import com.gnims.project.domain.friendship.dto.*;
import com.gnims.project.domain.friendship.service.FriendshipService;
import com.gnims.project.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static com.gnims.project.domain.friendship.entity.FollowStatus.INIT;
import static com.gnims.project.share.message.ResponseMessage.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.*;


@RestController
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final ApplicationEventPublisher applicationEventPublisher;

    // 팔로잉 조회(내가 등록한 친구)
    @GetMapping("/friendship/followings")
    public ResponseEntity<FriendshipResult> readFollowing(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @RequestParam(defaultValue = "0") Integer page,
                                                          @RequestParam(defaultValue = "9999") Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<FollowReadResponse> followings = friendshipService.readFollowing(userDetails.receiveUserId(), pageRequest);

        return ok(new FriendshipResult(200, READ_FOLLOWINGS_MESSAGE, followings));
    }
    //개선 버전 팔로잉 및 팔로잉 수 함께 조회
    @GetMapping("/v2/friendship/followings")
    public ResponseEntity<FriendshipResult> readFollowingV2(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @RequestParam(defaultValue = "0") Integer page,
                                                          @RequestParam(defaultValue = "9999") Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<FollowReadResponse> followings = friendshipService.readFollowing(userDetails.receiveUserId(), pageRequest);
        FollowIntegratedResponse response = FollowIntegratedResponse.of(followings, followings.size());

        return ok(new FriendshipResult(200, READ_FOLLOWINGS_MESSAGE, response));
    }


    //팔로워 조회(나를 등록한 친구)
    @GetMapping("/friendship/followers")
    public ResponseEntity<FriendshipResult> readFollower(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @RequestParam(defaultValue = "0") Integer page,
                                                         @RequestParam(defaultValue = "9999") Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<FollowReadResponse> followers = friendshipService.readFollower(userDetails.receiveUserId(), pageRequest);

        return ok(new FriendshipResult(200, READ_FOLLOWERS_MESSAGE, followers));
    }
    //개선 버전 팔로워 및 팔로워 수 함께 조회
    @GetMapping("/v2/friendship/followers")
    public ResponseEntity<FriendshipResult> readFollowerV2(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @RequestParam(defaultValue = "0") Integer page,
                                                         @RequestParam(defaultValue = "9999") Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<FollowReadResponse> followers = friendshipService.readFollower(userDetails.receiveUserId(), pageRequest);
        FollowIntegratedResponse response = FollowIntegratedResponse.of(followers, followers.size());

        return ok(new FriendshipResult(200, READ_FOLLOWERS_MESSAGE, response));
    }
    // 팔로잉 하기/취소
    @PostMapping("/friendship/followings/{followings-id}")
    public ResponseEntity<FriendshipResult> createFriendShip(@PathVariable("followings-id") Long followingId,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        FriendshipResponse response = friendshipService.makeFriendship(userDetails.receiveUserId(), followingId);
        FriendShipCreatedEvent serviceResponse = response.to(userDetails.receiveUserId(), userDetails.getUsername());

        if (response.getStatus().equals(INIT)) {
            applicationEventPublisher.publishEvent(serviceResponse);
            return new ResponseEntity<>(new FriendshipResult(CREATED.value(), response.receiveStatusMessage(), response), CREATED);
        }

        return ok(new FriendshipResult(OK.value(), response.receiveStatusMessage(), response));
    }

    //팔로잉 수
    @GetMapping("/friendship/followings/counting")
    public ResponseEntity<FriendshipResult> countFollowing(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Integer count = friendshipService.countFollowing(userDetails.receiveUserId());

        return ok(new FriendshipResult(OK.value(), COUNT_FOLLOWINGS_MESSAGE, count));
    }

    //팔로워 수
    @GetMapping("/friendship/followers/counting")
    public ResponseEntity<FriendshipResult> countFollower(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Integer count = friendshipService.countFollower(userDetails.receiveUserId());

        return ok(new FriendshipResult(OK.value(), COUNT_FOLLOWERS_MESSAGE, count));
    }
}
