package com.gnims.project.share.gmail;

import com.gnims.project.share.persistence.superclass.TimeStamped;
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
    private String code;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private Boolean isChecked = false;

    public EmailValidation(String code, String email) {
        this.code = code;
        this.email = email;
    }

    public void isCheckedTrue() {
        this.isChecked = true;
    }
}
