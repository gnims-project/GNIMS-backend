package com.gnims.project.domain.notification.repository;

import com.gnims.project.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdOrderByCreateAtDesc(Long userId);
}
