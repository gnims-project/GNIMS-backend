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

    public void create(Long accepterId, Long senderId, String message) {
        User accepter = userRepository.findById(accepterId).get();
        Notification notification = new Notification(accepter.getUsername(), message, senderId);
        notificationRepository.save(notification);
    }
}
