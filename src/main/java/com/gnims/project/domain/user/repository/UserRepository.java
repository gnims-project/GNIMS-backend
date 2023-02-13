package com.gnims.project.domain.user.repository;

import com.gnims.project.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByNickname(String nickname);

    //정규식으로 searching
    @Query(value = "select * From users where (nickname REGEXP :searchKeyword) order by id desc ", nativeQuery = true)
    Page<User> searchByRegExpKeyword(@Param("searchKeyword") String searchKeyword, @Param("pageRequest") Pageable pageable);

//    Page<User> searchByRegExpKeyword(String searchKeyword, Pageable pageable);
}
