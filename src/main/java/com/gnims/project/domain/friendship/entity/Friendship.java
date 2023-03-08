package com.gnims.project.domain.friendship.entity;

import com.gnims.project.domain.user.entity.User;
import com.gnims.project.share.persistence.superclass.TimeStamped;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.gnims.project.domain.friendship.entity.FollowStatus.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship extends TimeStamped {

    @Id @Column(name = "friendship_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //자기 자신
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "myself_id")
    private User myself;

    //팔로우 등록할 친구
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private User follow;

    @Enumerated(value = EnumType.STRING)
    private FollowStatus status;

    public Friendship(User myself, User follow) {
        this.myself = myself;
        this.follow = follow;
        this.status = INIT;
    }

    public String receiveFollowUsername() {
        return this.follow.getUsername();
    }

    public Long receiveFollowId() {
        return this.follow.getId();
    }

    public String receiveFollowProfile() {
        return this.follow.getProfileImage();
    }

    public boolean isActive() {
        return !this.status.equals(INACTIVE);
    }

    public void changeStatus(FollowStatus status) {
        this.status = status;
    }

    public boolean matchFollow(Long userId) {
        return userId.equals(this.follow.getId());
    }
}
