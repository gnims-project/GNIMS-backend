package com.gnims.project.domain.friendship.service;

import com.gnims.project.domain.friendship.dto.FollowingResponse;
import com.gnims.project.domain.friendship.entity.Friendship;
import com.gnims.project.domain.friendship.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import static com.gnims.project.domain.friendship.entity.FriendshipStatus.*;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    public List<FollowingResponse> readFollowing(Long myId) {
        List<Friendship> follows = friendshipRepository.findAllByCreateByAndStatusIs(myId, ACTIVE);
        return follows.stream().map(follow -> new FollowingResponse(follow.getFollower().getId(), follow.receiveUsername()))
                               .collect(Collectors.toList());
    }
}
