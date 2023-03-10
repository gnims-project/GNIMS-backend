package com.gnims.project.domain.friendship.service;

import com.gnims.project.domain.friendship.dto.FriendshipResponse;
import com.gnims.project.domain.friendship.dto.FollowReadResponse;
import com.gnims.project.domain.friendship.entity.FollowStatus;
import com.gnims.project.domain.friendship.entity.Friendship;
import com.gnims.project.domain.friendship.repository.FriendshipRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import com.gnims.project.exception.NotFoundInformationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.gnims.project.domain.friendship.entity.FollowStatus.*;
import static com.gnims.project.share.message.ExceptionMessage.BAD_ACCESS;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public List<FollowReadResponse> readFollowing(Long myselfId, PageRequest pageRequest) {
        List<Friendship> followings = friendshipRepository.readAllFollowingOf(myselfId, pageRequest);
        return followings.stream().map(f -> new FollowReadResponse(
                f.receiveFollowId(),
                f.receiveFollowUsername(),
                f.receiveFollowProfile(),
                f.getStatus())).collect(toList());
    }

    public List<FollowReadResponse> readFollower(Long myselfId, PageRequest pageRequest) {
        List<FollowReadResponse> followers = friendshipRepository.readAllFollowerOf(myselfId, pageRequest);
        return followers.stream().map(f -> new FollowReadResponse(
                        f.getFollowId(),
                        f.getUsername(),
                        f.getProfile(),
                        sendFollowStatus(f.getFollowStatus()))).collect(toList());
    }

    private FollowStatus sendFollowStatus(FollowStatus status) {
        return status == null ? INACTIVE : status;
    }

    @Transactional
    public FriendshipResponse makeFriendship(Long myselfId, Long followingId) {
        // 팔로잉 했는지 확인
        Optional<Friendship> optionalFriendship = friendshipRepository.findAllByMyselfIdAndFollowId(myselfId, followingId);
        // 한 번도 팔로잉을 한적이 없다면
        if (optionalFriendship.isEmpty()) {
            User user = userRepository.findById(followingId)
                    .orElseThrow(() -> new NotFoundInformationException(BAD_ACCESS));
            User myself = userRepository.findById(myselfId)
                    .orElseThrow(() -> new NotFoundInformationException(BAD_ACCESS));
            Friendship friendship = friendshipRepository.save(new Friendship(myself, user));
            return new FriendshipResponse(friendship.receiveFollowId(), friendship.getStatus());
        }
        // 한번이라도 팔로잉 관계를 맺은 적이 있다면
        Friendship friendship = optionalFriendship.get();

        if (friendship.isActive()) {
            friendship.changeStatus(INACTIVE);
            return new FriendshipResponse(friendship.receiveFollowId(), friendship.getStatus());
        }

        friendship.changeStatus(ACTIVE);
        return new FriendshipResponse(friendship.receiveFollowId(), friendship.getStatus());
    }

    public Integer countFollowing(Long myselfId) {
        return friendshipRepository.countAllByMyselfIdAndStatusNot(myselfId, INACTIVE);
    }

    public Integer countFollower(Long myselfId) {
        return friendshipRepository.countAllByFollowIdAndStatusNot(myselfId, INACTIVE);
    }
}
