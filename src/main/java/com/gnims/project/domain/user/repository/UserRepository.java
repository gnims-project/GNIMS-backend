package com.gnims.project.domain.user.repository;

import com.gnims.project.domain.user.dto.SearchAllQueryDto;
import com.gnims.project.domain.user.entity.User;
import org.springframework.data.domain.Page;
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

    //정규식으로 searching
    @Query(value = "select * From users where (search_nickname REGEXP :searchKeyword) order by id desc ", nativeQuery = true)
    Page<User> searchByRegExpKeyword(@Param("searchKeyword") String searchKeyword, @Param("pageRequest") Pageable pageable);

//    Page<User> searchByRegExpKeyword(String searchKeyword, Pageable pageable);

    Optional<List<User>> findAllByUsernameLikeAndUsernameIsNot(String username, String myName, Pageable pageable);

    @Query(value = "select new com.gnims.project.domain.user.dto.SearchAllQueryDto" +
            "(u.id, u.username, u.profileImage, (case when f.status is null or f.status = 'INACTIVE' then false else true end)) from users u " +
            "left outer join Friendship f on f.myself.id = :myId " +
            "where u.username <> :myName and u.username like :username")
    List<SearchAllQueryDto> testsearch1(@Param("username") String username, @Param("myName") String myName, @Param("myId") Long myId, @Param("pageRequest") Pageable pageable);
}
