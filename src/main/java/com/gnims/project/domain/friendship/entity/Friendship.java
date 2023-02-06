package com.gnims.project.domain.friendship.entity;

import com.gnims.project.domain.user.entity.User;
import com.gnims.project.util.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.gnims.project.domain.friendship.entity.FollowStatus.*;

@Getter
@Entity
@NoArgsConstructor
public class Friendship extends BaseEntity {

    @Id @Column(name = "friendship_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;

    @Enumerated(value = EnumType.STRING)
    private FollowStatus status;

    public Friendship(User follower) {
        this.follower = follower;
        this.status = INIT;
    }

    public String receiveUsername() {
        return this.follower.getUsername();
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
        return this.follower.getId();
    }

}
