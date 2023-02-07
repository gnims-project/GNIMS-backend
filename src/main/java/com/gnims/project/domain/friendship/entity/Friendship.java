package com.gnims.project.domain.friendship.entity;

import com.gnims.project.domain.user.entity.User;
import com.gnims.project.util.TimeStamped;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.gnims.project.domain.friendship.entity.FollowStatus.*;

@Getter
@Entity
@NoArgsConstructor
public class Friendship extends TimeStamped {

    @Id @Column(name = "friendship_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //자기 자신
    @ManyToOne
    @JoinColumn(name = "myself_id")
    private User myself;

    //팔로우 등록할 친구
    @ManyToOne
    @JoinColumn(name = "following_id")
    private User following;

    @Enumerated(value = EnumType.STRING)
    private FollowStatus status;

    public Friendship(User myself, User following) {
        this.myself = myself;
        this.following = following;
        this.status = INIT;
    }

    public String receiveFollowingUsername() {
        return this.following.getUsername();
    }

    public String receiveMyselfUsername() {
        return this.myself.getUsername();
    }

    public Long receiveMyselfId() {
        return this.myself.getId();
    }

    public boolean isActive() {
        if (this.status.equals(INACTIVE)) {
            return false;
        }

        return true;
    }

    public void changeStatus(FollowStatus status) {
        this.status = status;
    }

    public Long receiveFollowId() {
        return this.following.getId();
    }

}
