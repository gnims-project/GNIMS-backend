package com.gnims.project.domain.user.dto;

import com.gnims.project.domain.user.entity.SocialCode;
import com.gnims.project.share.validation.ValidationGroups;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.gnims.project.share.message.ExceptionMessage.EMAIL_EMPTY_MESSAGE;
import static com.gnims.project.share.message.ExceptionMessage.EMAIL_ERROR_MESSAGE;

@Getter
public class EmailDto {

    @NotBlank(message = EMAIL_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    //더 디테일한 체크를 위해 @Email 대신 @Pattern 사용
    @Pattern(regexp = "^[a-zA-Z0-9+-_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = EMAIL_ERROR_MESSAGE)
    private String email;
    private SocialCode socialCode;
}
