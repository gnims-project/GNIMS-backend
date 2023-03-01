package com.gnims.project.domain.event.repository;

import com.gnims.project.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findBySubject(String subject);
    Optional<Event> findByCreateByAndId(Long createBy, Long eventId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Event e set e.dDay = e.dDay - 1")
    void updateDDay();
}
