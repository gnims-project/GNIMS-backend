package com.gnims.project.domain.event.repository;

import com.gnims.project.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findBySubject(String subject);
    Optional<Event> findByCreateByAndId(Long createBy, Long eventId);
}
