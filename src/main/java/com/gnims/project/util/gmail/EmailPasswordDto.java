package com.gnims.project.util.gmail;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailPasswordDto {

    private String link;
    private String email;
    private String password;

    public EmailPasswordDto(String link, String email, String password) {
        this.link = link;
        this.email = email;
        this.password = password;
    }
}
