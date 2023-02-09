package com.gnims.project.domain.schedule.repository;

import com.gnims.project.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByEvent_IdAndIsAcceptedIs(Long eventId, Boolean isAccepted);
    List<Schedule> findAllByUser_IdAndIsAcceptedIs(Long userId, Boolean isAccepted);

    Optional<Schedule> findByUser_IdAndEvent_Id(Long userId, Long eventId);
}
