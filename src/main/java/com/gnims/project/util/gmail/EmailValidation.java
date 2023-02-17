package com.gnims.project.util.gmail;

import com.gnims.project.util.TimeStamped;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class EmailValidation extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id")
    private Long id;
    @Column(nullable = false)
    private String link;

    public EmailValidation(String link) {
        this.link = link;
    }
}
