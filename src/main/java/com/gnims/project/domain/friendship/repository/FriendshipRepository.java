package com.gnims.project.domain.friendship.repository;

import com.gnims.project.domain.friendship.entity.Friendship;
import com.gnims.project.domain.friendship.entity.FollowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findAllByMyself_IdAndFollow_Id(Long myselfId, Long followId);
    Integer countAllByMyself_IdAndStatusNot(Long myselfId, FollowStatus status);
    Integer countAllByFollow_IdAndStatusNot(Long followId, FollowStatus status);

    @Query(value = "select f from Friendship f " +
            "join fetch f.follow " +
            "where f.myself.id =:userId " +
            "and not f.status = com.gnims.project.domain.friendship.entity.FollowStatus.INACTIVE")
    List<Friendship> readAllFollowingOf(Long userId);

    @Query(value = "select f from Friendship f " +
            "join fetch f.follow " +
            "where f.follow.id =:userId " +
            "and not f.status = com.gnims.project.domain.friendship.entity.FollowStatus.INACTIVE")
    List<Friendship> readAllFollowerOf(Long userId);

}
