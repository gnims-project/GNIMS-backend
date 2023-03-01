package com.gnims.project.domain.notification.entity;

import com.gnims.project.domain.user.entity.User;
import com.gnims.project.share.persistence.superclass.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import static javax.persistence.EnumType.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @Column(name = "notification_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private boolean isChecked;

    @Enumerated(value = STRING)
    private NotificationType notificationType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepter_id")
    private User user;

    public Notification(String message, User user) {
        this.message = message;
        this.user = user;
        this.isChecked = false;
    }

    public void decideNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public void isRead() {
        this.isChecked = true;
    }
    // 응답 내려주기용 is를 인식못함
    public boolean getIsChecked() {
        return isChecked;
    }
}
