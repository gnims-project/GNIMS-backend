package com.gnims.project.domain.friendship.repository;

import com.gnims.project.domain.friendship.entity.Friendship;
import com.gnims.project.domain.friendship.entity.FollowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findAllByMyself_IdAndStatusNot(Long myselfId, FollowStatus status);
    Optional<Friendship> findAllByMyself_IdAndFollowing_Id(Long myselfId, Long followingId);
    List<Friendship> findAllByFollowing_Id(Long myselfId);
    Page<Friendship> findAllByMyself_IdAndStatusNot(Long myselfId, FollowStatus status, Pageable pageable);
}
