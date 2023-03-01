package com.gnims.project.domain.schedule.dto;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class UpdateForm {
    private LocalDate date;
    private LocalTime time;
    @NotBlank(message = "제목은 필수입니다.")
    @Length(max = 20, message = "제목은 20자 이하로 작성해주세요.")
    private String subject;
    private String content;
    private String cardColor;
}
