package com.gnims.project.domain.notification.entity;

import com.gnims.project.share.persistence.superclass.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    private Long accepterId;

    public Notification(String message, Long accepterId) {
        this.message = message;
        this.accepterId = accepterId;
        this.isChecked = false;
    }

    public void changeIsChecked(boolean checked) {
        this.isChecked = checked;
    }
    // 응답 내려주기용 is를 인식못함
    public boolean getIsChecked() {
        return isChecked;
    }
}
