package com.gnims.project.share.email;

import lombok.Getter;

@Getter
public class AuthCodeDto {
    private String code;
    private String email;
}
