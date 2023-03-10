package com.gnims.project.domain.user.repository;

import com.gnims.project.domain.user.dto.SearchAllQueryDto;
import com.gnims.project.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByNickname(String nickname);

    //최적화
    @Query(value = "select new com.gnims.project.domain.user.dto.SearchAllQueryDto" +
            "(u.id, u.username, u.profileImage, (case when f.status is null or f.status = 'INACTIVE' then false else true end)) from users u " +
            "left outer join Friendship f on (f.myself.id = :myId and f.follow.id = u.id)" +
            "where u.id <> :myId and u.username like :username")
    List<SearchAllQueryDto> userSearch(@Param("username") String username, @Param("myId") Long myId, @Param("pageRequest") Pageable pageable);
}
