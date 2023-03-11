package com.gnims.project.share.email;

import com.gnims.project.share.validation.ValidationGroups;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.gnims.project.share.message.ExceptionMessage.SECRET_EMPTY_MESSAGE;
import static com.gnims.project.share.message.ExceptionMessage.SECRET_ERROR_MESSAGE;

@Getter
public class EmailPasswordDto {
    private String email;

    @NotBlank(message = SECRET_EMPTY_MESSAGE,
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = SECRET_ERROR_MESSAGE)
    private String password;
}
