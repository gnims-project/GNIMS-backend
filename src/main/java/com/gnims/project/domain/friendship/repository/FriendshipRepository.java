package com.gnims.project.domain.friendship.repository;

import com.gnims.project.domain.friendship.entity.Friendship;
import com.gnims.project.domain.friendship.entity.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findAllByCreateByAndStatusIs(Long id, FriendshipStatus status);
    Optional<Friendship> findByFollower_IdAndCreateBy(Long followerId, Long MyId);
}
