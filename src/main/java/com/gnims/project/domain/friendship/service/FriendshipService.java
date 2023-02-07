package com.gnims.project.domain.friendship.service;

import com.gnims.project.domain.friendship.dto.FollowResponse;
import com.gnims.project.domain.friendship.dto.FollowingResponse;
import com.gnims.project.domain.friendship.entity.Friendship;
import com.gnims.project.domain.friendship.repository.FriendshipRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import static com.gnims.project.domain.friendship.entity.FollowStatus.*;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public List<FollowingResponse> readFollowing(Long myselfId) {
        List<Friendship> follows = friendshipRepository.findAllByMyself_IdAndStatusNot(myselfId, INACTIVE);
        return follows.stream().map(follow -> new FollowingResponse(follow.getFollowing().getId(), follow.receiveFollowingUsername()))
                               .collect(toList());
    }

    @Transactional
    public FollowResponse clickFollowButton(Long myselfId, Long followingId) {
        // 팔로잉 했는지 확인
        Optional<Friendship> optionalFriendship = friendshipRepository.findAllByMyself_IdAndFollowing_Id(myselfId, followingId);

        // 한 번도 팔로잉을 한적이 없다면
        if (optionalFriendship.isEmpty()) {
            User user = userRepository.findById(followingId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
            User myself = userRepository.findById(myselfId).get();
            Friendship friendship = friendshipRepository.save(new Friendship(myself, user));
            return new FollowResponse(friendship.receiveFollowId(), friendship.getStatus());
        }

        // 한번이라도 팔로잉 관계를 맺은 적이 있다면
        Friendship friendship = optionalFriendship.get();

        if (friendship.isActive()) {
            friendship.changeStatus(INACTIVE);
            return new FollowResponse(friendship.receiveFollowId(), friendship.getStatus());
        }

        friendship.changeStatus(ACTIVE);
        return new FollowResponse(friendship.receiveFollowId(), friendship.getStatus());
    }

    // 프로토 타입 - 리스트 조회 최적화 필요.
    public List<FollowingResponse> readFollower(Long myId) {
        List<Friendship> followers = friendshipRepository.findAllByFollowing_Id(myId);

        return followers.stream().map(f -> new FollowingResponse(f.receiveMyselfId(), f.receiveMyselfUsername()))
                .collect(toList());
    }
}
