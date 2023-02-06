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
import java.util.stream.Collectors;

import static com.gnims.project.domain.friendship.entity.FollowStatus.*;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public List<FollowingResponse> readFollowing(Long myId) {
        List<Friendship> follows = friendshipRepository.findAllByCreateByAndStatusNot(myId, INACTIVE);
        return follows.stream().map(follow -> new FollowingResponse(follow.getFollower().getId(), follow.receiveUsername()))
                               .collect(Collectors.toList());
    }

    @Transactional
    public FollowResponse clickFollowButton(Long myId, Long userId) {
        // 팔로잉 했는지 확인
        Optional<Friendship> optionalFriendship = friendshipRepository.findByFollower_IdAndCreateBy(userId, myId);

        // 한 번도 팔로잉을 한적이 없다면
        if (optionalFriendship.isEmpty()) {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

            Friendship friendship = friendshipRepository.save(new Friendship(user));
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
}
