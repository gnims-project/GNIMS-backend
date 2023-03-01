package com.gnims.project.domain.notification.service;

import com.gnims.project.domain.notification.dto.ReadAllNotificationResponse;
import com.gnims.project.domain.notification.entity.Notification;
import com.gnims.project.domain.notification.entity.NotificationType;
import com.gnims.project.domain.notification.repository.NotificationRepository;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.gnims.project.share.message.ExceptionMessage.NOT_EXISTED_NOTIFICATION;
import static com.gnims.project.share.message.ExceptionMessage.NOT_EXISTED_USER;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Notification create(Long accepterId, String message, NotificationType notificationType) {
        User user = userRepository.findById(accepterId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_EXISTED_USER));

        Notification notification = new Notification(message, user);
        notification.decideNotificationType(notificationType);

        return notificationRepository.save(notification);
    }

    public List<ReadAllNotificationResponse> readAll(Long userId) {
        List<Notification> notifications = notificationRepository.findAllByUser_Id(userId);

        return notifications.stream().map(n -> new ReadAllNotificationResponse(
                n.getId(),
                n.getCreateAt(),
                n.getCreateBy(),
                n.getMessage(),
                n.getIsChecked(),
                n.getNotificationType())).collect(toList());
    }

    @Transactional
    public void readAndCheckNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_EXISTED_NOTIFICATION));

        notification.isRead();
    }

}
