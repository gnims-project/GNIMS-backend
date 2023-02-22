package com.gnims.project.domain.notification.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @Column(name = "notification_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderName;

    private String message;

    private boolean isChecked;

    private Long accepterId;

    public Notification(String senderName, String message, Long accepterId) {
        this.senderName = senderName;
        this.message = message;
        this.accepterId = accepterId;
        this.isChecked = false;
    }

    public void changeIsChecked(boolean checked) {
        this.isChecked = checked;
    }
}
