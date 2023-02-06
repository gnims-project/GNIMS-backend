package com.gnims.project.domain.friendship.service;

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

import static com.gnims.project.domain.friendship.entity.FriendshipStatus.*;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public List<FollowingResponse> readFollowing(Long myId) {
        List<Friendship> follows = friendshipRepository.findAllByCreateByAndStatusIs(myId, ACTIVE);
        return follows.stream().map(follow -> new FollowingResponse(follow.getFollower().getId(), follow.receiveUsername()))
                               .collect(Collectors.toList());
    }

    @Transactional
    public String clickFollowButton(Long myId, Long followerId) {
        // 팔로잉 했는지 확인
        Optional<Friendship> optionalFollow = friendshipRepository.findByFollower_IdAndCreateBy(followerId, myId);

        // 한 번도 팔로잉을 한적이 없다면
        if (optionalFollow.isEmpty()) {
            User user = userRepository.findById(followerId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

            Friendship follow = new Friendship(user);
            Friendship findFollow = friendshipRepository.save(follow);
            return findFollow.receiveUsername() + "님을 팔로잉합니다.";
        }

        // 한번이라도 팔로잉 관계를 맺은 적이 있다면
        Friendship follow = optionalFollow.get();

        if (follow.isActive()) {
            follow.changeStatus(INACTIVE);
            return "팔로잉 취소";
        }

        follow.changeStatus(ACTIVE);
        return "다시 팔로잉";
    }
}
