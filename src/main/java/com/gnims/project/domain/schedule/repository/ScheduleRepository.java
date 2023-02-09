package com.gnims.project.domain.schedule.repository;

import com.gnims.project.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByUser_Id(Long userId);
    List<Schedule> findAllByEvent_Id(Long eventId);
}
