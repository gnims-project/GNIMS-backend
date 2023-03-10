package com.gnims.project.share.validation;

import com.gnims.project.share.validation.ValidationGroups.NotNullGroup;
import com.gnims.project.share.validation.ValidationGroups.PatternCheckGroup;

import javax.validation.GroupSequence;

//유효성 검사의 우선 순위를 지정 빈값 -> 패턴 체크
@GroupSequence({NotNullGroup.class, PatternCheckGroup.class})
public interface ValidationSequence {
}
