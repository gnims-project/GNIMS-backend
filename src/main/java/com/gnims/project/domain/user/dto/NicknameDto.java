package com.gnims.project.domain.user.dto;

import com.gnims.project.util.validation.ValidationGroups;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
public class NicknameDto {

    @NotBlank(message = "닉네임을 적어 주세요",
            groups = ValidationGroups.NotNullGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,8}$",
            groups = ValidationGroups.PatternCheckGroup.class,
            message = "특수문자를 제외한 2 ~ 8 자리의 닉네임만 가능합니다.")
    private String nickname;
}
