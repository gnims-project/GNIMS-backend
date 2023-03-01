package com.gnims.project.share.gmail;

import com.gnims.project.share.validation.ValidationGroups;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.gnims.project.share.message.ExceptionMessage.PASSWORD_EMPTY_MESSAGE;
import static com.gnims.project.share.message.ExceptionMessage.PASSWORD_ERROR_MESSAGE;

@Getter
public class EmailPasswordDto {
    private String email;
    @NotBlank(message = PASSWORD_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = PASSWORD_ERROR_MESSAGE)
    private String password;
}
