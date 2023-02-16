package com.gnims.project.domain.notification.entity;

import com.gnims.project.domain.user.entity.User;
import com.gnims.project.util.TimeStamped;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * 알림 Entity
 */

/** to 알람 적용하는 곳에
 *        //댓글 생성 시 게시글 작성 유저에게 실시간 알림 전송 ,
 *         Schedule schedule = schedules.get(0);
 *
 *         String message = schedule.getUser().getUsername() + "님! 게시물에 작성된 댓글 알림이 도착했어요!";
 * //                "확인하러가기 https://www.chackcheck99.com/detail/" + schedule.getEvent().getId();
 *
 *         //본인의 게시글에 댓글을 남길때는 알림을 보낼 필요가 없다.
 *         notificationService.send(schedule.getUser(), AlarmType.comment, message, schedule.getEvent().getId(), schedule.getEvent().getSubject(), schedule.getEvent().getCreateAt());
 *         log.info("Alarm 대상 : {}, Alram 메시지 = {}", schedule.getUser().getUsername(), message);
 */

@Entity
@Getter @Setter
@NoArgsConstructor
public class Notification extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    /**
     * 알림 message
     */
    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Boolean readState;

    /**
     * 클릭 시 이동할 수 있는 link 필요
     */

    private Long url;

    /**
     * 멤버 변수이름 변경
     */
    @ManyToOne
    @JoinColumn(name = "receiver_user_id")
    private User user;

    @Column
    private String title;


    @Builder
    public Notification(AlarmType alarmType, String message, Boolean readState,
                        Long articlesId, User receiver, String title) {
        this.alarmType = alarmType;
        this.message = message;
        this.readState = readState;
        this.url = articlesId;
        this.user = receiver;
        this.title = title;
    }

    public void changeState() {
        readState = true;
    }




}

