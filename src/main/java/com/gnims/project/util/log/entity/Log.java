package com.gnims.project.util.log.entity;

import com.gnims.project.util.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Log extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_time_id")
    private Long id;
    private Long time;
    private String methodName;

    public Log(Long time, String methodName) {
        this.time = time;
        this.methodName = methodName;
    }
}
