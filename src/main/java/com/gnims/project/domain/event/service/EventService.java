package com.gnims.project.domain.event.service;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.event.repository.EventRepository;
import com.gnims.project.domain.schedule.dto.UpdateForm;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gnims.project.share.message.ExceptionMessage.ALREADY_DELETED_EVENT;
import static com.gnims.project.share.message.ExceptionMessage.ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    @Transactional
    public void softDeleteSchedule(Long userId, Long eventId) {
        Event event = eventRepository.findByCreateByAndId(userId, eventId)
                .orElseThrow(() -> new SecurityException(ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE));

        checkIsDeleted(event);
        event.removeEvent();
    }

    @Transactional
    public void updateSchedule(Long userId, UpdateForm updateForm, Long eventId) {
        Event event = eventRepository.findByCreateByAndId(userId, eventId).orElseThrow(
                () -> new SecurityException(ALREADY_PROCESSED_OR_NO_AUTHORITY_SCHEDULE));

        checkIsDeleted(event);
        event.updateEvent(updateForm);
    }

    public void checkIsDeleted(Event event) {
        if (event.getIsDeleted().equals(true)) {
            throw new IllegalArgumentException(ALREADY_DELETED_EVENT);
        }
    }
}
