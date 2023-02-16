package com.gnims.project.domain.notification.repository;

import com.gnims.project.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = "select n from Notification n where n.user.id = :id order by n.id desc")
    List<Notification> findAllByReceiver_UserId(Long id);

    void deleteAllByUser_Id(Long receiverId);


}

