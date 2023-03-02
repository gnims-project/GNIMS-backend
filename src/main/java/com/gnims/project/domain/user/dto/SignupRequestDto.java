package com.gnims.project.domain.user.dto;

import com.gnims.project.share.validation.ValidationGroups;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.gnims.project.share.message.ExceptionMessage.*;

@Getter
@NoArgsConstructor
public class SignupRequestDto {
    @NotBlank(message = NICKNAME_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,8}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = NICKNAME_ERROR_MESSAGE)
    private String nickname;

    @NotBlank(message = USERNAME_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣]{1,12}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = USERNAME_ERROR_MESSAGE)
    private String username;

    @NotBlank(message = EMAIL_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9+-_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = EMAIL_ERROR_MESSAGE)
    private String email;

    @NotBlank(message = SECRET_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = SECRET_ERROR_MESSAGE)
    private String password;

    public SignupRequestDto(String nickname, String username, String email, String password) {
        this.nickname = nickname;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
