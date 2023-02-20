package com.gnims.project.social.dto;

import com.gnims.project.domain.user.entity.SocialCode;
import com.gnims.project.util.validation.ValidationGroups;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.gnims.project.exception.dto.ExceptionMessage.*;

@Getter
public class SocialSignupDto {
    @NotBlank(message = NICKNAME_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,8}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = NICKNAME_ERROR_MESSAGE)
    private String nickname;

    @NotBlank(message = USERNAME_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣]{2,8}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = USERNAME_ERROR_MESSAGE)
    private String username;

    @NotBlank(message = EMAIL_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9+-_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = EMAIL_ERROR_MESSAGE)
    private String email;

    private SocialCode socialCode;
}
