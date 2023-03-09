package com.gnims.project.domain.notification.repository;

import com.gnims.project.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdOrderByCreateAtDesc(Long userId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.isChecked = true where n.user.id =:userId")
    void updateAllChecked(@Param("userId") Long userId);

    List<Notification> findAllByUserId(Long userId);
}
