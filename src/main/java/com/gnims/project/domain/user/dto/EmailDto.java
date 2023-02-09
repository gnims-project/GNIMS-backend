package com.gnims.project.domain.user.dto;

import com.gnims.project.domain.user.entity.SocialCode;
import com.gnims.project.util.validation.ValidationGroups;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
public class EmailDto {

    @NotBlank(message = "이메일을 적어 주세요",
            groups = ValidationGroups.NotNullGroup.class)
    //더 디테일한 체크를 위해 @Email 대신 @Pattern 사용
    @Pattern(regexp = "^[a-zA-Z0-9+-_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = "올바른 형식의 이메일 주소여야 합니다")
    private String email;
    private SocialCode socialCode;
}
