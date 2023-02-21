package com.gnims.project.util.gmail;

import com.gnims.project.util.validation.ValidationGroups;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
public class EmailPasswordDto {
    private String email;
    @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.",
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = "비밀번호는 영문/숫자를 포함하여 8~16자로 입력해야합니다.")
    private String password;
}
