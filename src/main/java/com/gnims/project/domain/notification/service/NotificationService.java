package com.gnims.project.domain.notification.service;

import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

//    public Notification create(Long accepterId, Long senderId, String message) {
//        User accepter = userRepository.findById(accepterId).get();
//        Notification notification = new Notification(accepter.getUsername(), message, senderId);
//        return notificationRepository.save(notification);
//    }

    public Notification createV2(Long accepterId, String message) {
        Notification notification = new Notification(message, accepterId);
        return notificationRepository.save(notification);
    }

}
