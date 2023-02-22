package com.gnims.project.domain.user.dto;

import com.gnims.project.share.validation.ValidationGroups;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.gnims.project.share.message.ExceptionMessage.NICKNAME_EMPTY_MESSAGE;
import static com.gnims.project.share.message.ExceptionMessage.NICKNAME_ERROR_MESSAGE;

@Getter
public class NicknameDto {

    @NotBlank(message = NICKNAME_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,8}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = NICKNAME_ERROR_MESSAGE)
    private String nickname;
}
