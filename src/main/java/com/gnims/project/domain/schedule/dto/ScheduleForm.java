package com.gnims.project.domain.schedule.dto;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ScheduleForm {
    private LocalDate date;
    private LocalTime time;
    @NotBlank(message = "제목은 필수입니다.")
    @Length(max = 20, message = "제목은 20자 이하로 작성해주세요.")
    private String subject;
    private String content;
    private String cardColor;
    @UniqueElements(message = "초대 목록에 동일 인물이 중복해서 존재합니다.")
    @Size(max = 5, message = "공동 스케줄은 최대 5명 까지 초대 가능합니다.")
    private List<Long> participantsId;

    public ScheduleServiceForm to(Long id, String username) {
        return new ScheduleServiceForm(id, username, this);
    }
}