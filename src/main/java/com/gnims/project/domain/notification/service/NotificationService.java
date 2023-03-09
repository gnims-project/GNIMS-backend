package com.gnims.project.domain.notification.service;

import com.gnims.project.domain.notification.dto.ReadNotificationResponse;
import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.entity.NotificationType;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.gnims.project.share.message.ExceptionMessage.*;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Notification create(Long createBy, Long accepterId, String message, NotificationType notificationType) {
        User user = userRepository.findById(accepterId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_EXISTED_USER));

        Notification notification = new Notification(user, createBy, message);

        notification.decideNotificationType(notificationType);

        return notificationRepository.save(notification);
    }

    public List<ReadNotificationResponse> readAll(Long userId) {
        List<Notification> notifications = notificationRepository.findAllByUserIdOrderByCreateAtDesc(userId);

        return notifications.stream().map(n -> new ReadNotificationResponse(
                n.getId(),
                n.getCreateAt(),
                n.getCreateBy(),
                n.getMessage(),
                n.getIsChecked(),
                n.getNotificationType())).collect(toList());
    }

    public void checkAll(Long userId) {
        notificationRepository.updateAllChecked(userId);
    }

    @Transactional
    public void readAndCheckNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByUserIdAndId(userId, notificationId)
                .orElseThrow(() -> new IllegalArgumentException(BAD_ACCESS));

        notification.isRead();
    }

}
