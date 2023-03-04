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


@RestController
@RequiredArgsConstructor
@Transactional
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final ApplicationEventPublisher applicationEventPublisher;

    // 팔로잉 조회(내가 등록한 친구)
    @GetMapping("/friendship/followings")
    public ResponseEntity<FriendshipResult> readFollowing(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @RequestParam(defaultValue = "0") Integer page,
                                                          @RequestParam(defaultValue = "9999") Integer size) {
        Long myselfId = userDetails.receiveUserId();
        PageRequest pageRequest = PageRequest.of(page, size);
        List<FollowReadResponse> followings = friendshipService.readFollowing(myselfId, pageRequest);

        return ResponseEntity.ok(new FriendshipResult(200, READ_FOLLOWINGS_MESSAGE, followings));
    }

    //팔로워 조회(나를 등록한 친구)
    @GetMapping("/friendship/followers")
    public ResponseEntity<FriendshipResult> readFollower(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @RequestParam(defaultValue = "0") Integer page,
                                                         @RequestParam(defaultValue = "9999") Integer size) {
        Long myselfId = userDetails.receiveUserId();
        PageRequest pageRequest = PageRequest.of(page, size);
        List<FollowReadResponse> followers = friendshipService.readFollower(myselfId, pageRequest);

        return ResponseEntity.ok(new FriendshipResult(200, READ_FOLLOWERS_MESSAGE, followers));
    }

    // 팔로잉 하기/취소
    @PostMapping("/friendship/followings/{followings-id}")
    public ResponseEntity<FriendshipResult> createFriendShip(@PathVariable("followings-id") Long followingId,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long myselfId = userDetails.receiveUserId();
        FriendshipResponse response = friendshipService.makeFriendship(myselfId, followingId);
        FriendShipServiceResponse serviceResponse = response.convertServiceResponse(myselfId, userDetails.getUser().getUsername());

        if (response.getStatus().equals(INIT)) {
            applicationEventPublisher.publishEvent(serviceResponse);
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
