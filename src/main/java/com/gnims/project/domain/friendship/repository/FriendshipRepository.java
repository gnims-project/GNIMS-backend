package com.gnims.project.domain.friendship.repository;

import com.gnims.project.domain.friendship.dto.FollowReadResponse;
import com.gnims.project.domain.friendship.entity.Friendship;
import com.gnims.project.domain.friendship.entity.FollowStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findAllByMyself_IdAndFollow_Id(Long myselfId, Long followId);

    @Query(value = "select f from Friendship f " +
            "join fetch f.follow " +
            "where f.myself.id =:myselfId " +
            "and f.follow.id =:followId")
    Optional<Friendship> findFriendShip(@Param("myselfId") Long myselfId, @Param("followId")Long followId);

    Integer countAllByMyself_IdAndStatusNot(Long myselfId, FollowStatus status);
    Integer countAllByFollow_IdAndStatusNot(Long followId, FollowStatus status);

    @Query(value = "select f from Friendship f " +
            "join fetch f.follow " +
            "where f.myself.id =:userId " +
            "and not f.status = com.gnims.project.domain.friendship.entity.FollowStatus.INACTIVE")
    List<Friendship> readAllFollowingOf(@Param("userId")Long userId);

    @Query(value = "select f from Friendship f " +
            "join fetch f.follow " +
            "where f.follow.id =:userId " +
            "and not f.status = com.gnims.project.domain.friendship.entity.FollowStatus.INACTIVE")
    List<Friendship> readAllFollowerOfProto(@Param("userId") Long userId);

    @Query(value = "select new com.gnims.project.domain.friendship.dto.FollowReadResponse" +
            "(f.myself.id, f.myself.username, f.myself.profileImage, f2.status) from Friendship f " +
            "left outer join Friendship f2 on f2.follow.id = f.myself.id and f2.myself.id = f.follow.id " +
            "where f.follow.id =:userId " +
            "and not f.status = com.gnims.project.domain.friendship.entity.FollowStatus.INACTIVE")
    List<FollowReadResponse> readAllFollowerOf(@Param("userId") Long userId);

    // 페이징 최적화 필요
    @Query(value = "select f from Friendship f " +
            "where f.follow.id =:userId " +
            "and not f.status = com.gnims.project.domain.friendship.entity.FollowStatus.INACTIVE")
    List<Friendship> readAllFollowerPageOf(@Param("userId") Long userId, PageRequest pageRequest);

    // 페이징 최적화 필요
    @Query(value = "select f from Friendship f " +
            "where f.myself.id =:userId " +
            "and not f.status = com.gnims.project.domain.friendship.entity.FollowStatus.INACTIVE")
    List<Friendship> readAllFollowingPageOf(@Param("userId") Long userId, PageRequest pageRequest);

}
