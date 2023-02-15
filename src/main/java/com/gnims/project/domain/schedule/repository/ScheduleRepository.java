package com.gnims.project.domain.schedule.repository;

import com.gnims.project.domain.event.entity.Event;
import com.gnims.project.domain.schedule.dto.EventOneQueryDto;
import com.gnims.project.domain.schedule.entity.Schedule;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByEvent_IdAndIsAcceptedIs(Long eventId, Boolean isAccepted);
    List<Schedule> findAllByUser_IdAndIsAcceptedIsAndEvent_IsDeletedIs(Long userId, Boolean isAccepted, Boolean isDeleted);
    List<Schedule> findAllByUser_IdAndIsAcceptedIs(Long userId, Boolean isAccepted);

    Optional<Schedule> findByUser_IdAndEvent_Id(Long userId, Long eventId);

    //현재 최적화
    @Query(value = "select s from Schedule s " +
            "join fetch s.user as u " +
            "where s.user.id = :userId and s.isAccepted = true and s.event.isDeleted = false")
    List<Schedule> readAllScheduleV2(Long userId);

    /**
     * 전체 조회 페이징 처리
     * user 는 4명 제한이기 때문에 패치 조인
     */
    @Query(value = "select s from Schedule s " +
            "join fetch s.user as u " +
            "where s.user.id = :userId and s.isAccepted = true and s.event.isDeleted = false")
    List<Schedule> readAllScheduleV2Pageable(Long userId, PageRequest pageRequest);

    /**
     * unique 한 값을 얻기 위해서 파라미터를 하나 더 받아야 한다.
     */
    @Query(value = "select s from Schedule s " +
            "join fetch s.event as e " +
            "join fetch s.user u " +
            "where e.id = :eventId and u.id = :userId and s.isAccepted = true and e.isDeleted = false")
    Optional<Schedule> readOneSchedule(Long userId, Long eventId);

    @Query(value = "select e from Event e " +
            "join fetch e.schedule s " +
            "join fetch s.user u " +
            "where e.id = :eventId and s.isAccepted = true and e.isDeleted = false")
    List<Event> readOneScheduleV3(Long eventId);

    /**
     * 단건 조회 최적화
     */
    @Query(value = "select new com.gnims.project.domain.schedule.dto.EventOneQueryDto" +
            "(e.id, e.appointment.date, e.appointment.time, e.cardColor, e.subject, e.content, u.username) from Event e " +
            "join e.schedule s " +
            "join s.user u " +
            "where e.id = :eventId and s.isAccepted = true and e.isDeleted = false")
    List<EventOneQueryDto> readOneScheduleV2Dto(Long eventId);

}
