package com.gnims.project.domain.schedule.repository;

import com.gnims.project.domain.schedule.dto.ReadAllScheduleDto;
import com.gnims.project.domain.schedule.dto.ReadOneScheduleDto;
import com.gnims.project.domain.schedule.dto.ReadPendingDto;
import com.gnims.project.domain.schedule.entity.Schedule;
import com.gnims.project.domain.schedule.entity.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByEvent_IdAndScheduleStatusIs(Long eventId, ScheduleStatus scheduleStatus);
    List<Schedule> findAllByUser_IdAndScheduleStatusIsAndEvent_IsDeletedIs(Long userId, ScheduleStatus scheduleStatus, Boolean isDeleted);
    List<Schedule> findAllByUser_IdAndScheduleStatusIs(Long userId, ScheduleStatus scheduleStatus);
    Optional<Schedule> findByUser_IdAndEvent_Id(Long userId, Long eventId);

    /**
     * 전체 조회 최적화 DTO -> 필요한 데이터를 한 번에 select
     * WHY? -> batch size, join fetch 를 적용해도 메모리 상에서 한 번 더 필터링을 하는 구조 때문에 지연 로딩이 초기화 되어 추가 쿼리가 나감
     * join을 통해 필요한 데이터를 한 번에 추출
     * subquery = 자신이 참여하는 EVENT_ID SELECT
     */

    @Query(value = "select new com.gnims.project.domain.schedule.dto.ReadAllScheduleDto" +
            "(e.id, e.appointment.date, e.appointment.time, e.cardColor, e.subject, e.dDay, u.username, u.profileImage) from Schedule s " +
            "join s.event e " +
            "join s.user u " +
            "where e.id in (select s2.event.id from Schedule s2 where s2.user.id =:userId " +
            "and s2.scheduleStatus = com.gnims.project.domain.schedule.entity.ScheduleStatus.ACCEPT) " +
            "and s.scheduleStatus = com.gnims.project.domain.schedule.entity.ScheduleStatus.ACCEPT " +
            "and e.isDeleted = false and e.dDay >= 0")
    List<ReadAllScheduleDto> readAllSchedule(@Param("userId") Long userId);


    @Query(value = "select u2.username, e.event_id, e.date, e.time, e.card_color, e.subject from schedule s " +
            "join users u on s.user_id = u.id " +
            "join event e on s.event_id = e.event_id " +
            "join users u2 on u2.id = e.create_by " +
            "where u.id =:userId and e.d_day >= 0 and s.schedule_status ='PENDING';",nativeQuery = true)
    List<ReadPendingDto> readAllPendingSchedule(@Param("userId") Long userId);

    /**
     * 전체 조회 페이징 처리
     * user 는 5명 제한이기 때문에 패치 조인 처리
     */

    @Query(value = "select new com.gnims.project.domain.schedule.dto.ReadAllScheduleDto" +
            "(e.id, e.appointment.date, e.appointment.time, e.cardColor, e.subject, e.dDay, u.username, u.profileImage) from Schedule s " +
            "join s.event e " +
            "join s.user u " +
            "where e.id in (select s2.event.id from Schedule s2 where s2.user.id =:userId and s2.scheduleStatus = com.gnims.project.domain.schedule.entity.ScheduleStatus.ACCEPT) " +
            "and s.scheduleStatus = com.gnims.project.domain.schedule.entity.ScheduleStatus.ACCEPT " +
            "and e.isDeleted = false and e.dDay >= 0")
    Page<ReadAllScheduleDto> readAllSchedulePage(@Param("userId") Long userId, PageRequest pageRequest);

    /**
     * 단건 조회 최적화
     */
    @Query(value = "select new com.gnims.project.domain.schedule.dto.ReadOneScheduleDto" +
            "(e.id, e.appointment.date, e.appointment.time, e.cardColor, e.subject, e.content, e.dDay, u.username) from Event e " +
            "join e.schedule s " +
            "join s.user u " +
            "where e.id = :eventId " +
            "and s.scheduleStatus = com.gnims.project.domain.schedule.entity.ScheduleStatus.ACCEPT " +
            "and e.isDeleted = false")
    List<ReadOneScheduleDto> readOneSchedule(@Param("eventId") Long eventId);

    //수락 및 거절을 대기중인 스케줄 조회
    @Query(value = "select s from Schedule s " +
            "where s.user.id = :userId and s.event.id = :eventId " +
            "and s.scheduleStatus = com.gnims.project.domain.schedule.entity.ScheduleStatus.PENDING " +
            "and s.event.isDeleted = false")
    Optional<Schedule> readOnePendingSchedule(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query(value = "select new com.gnims.project.domain.schedule.dto.ReadAllScheduleDto" +
            "(e.id, e.appointment.date, e.appointment.time, e.cardColor, e.subject, e.dDay, u.username, u.profileImage) from Schedule s " +
            "join s.event e " +
            "join s.user u " +
            "where e.id in (select s2.event.id from Schedule s2 where s2.user.id =:userId " +
            "and s2.scheduleStatus = com.gnims.project.domain.schedule.entity.ScheduleStatus.ACCEPT) " +
            "and s.scheduleStatus = com.gnims.project.domain.schedule.entity.ScheduleStatus.ACCEPT " +
            "and e.isDeleted = false and e.dDay < 0 order by e.appointment.date desc ")
    List<ReadAllScheduleDto> readPastSchedule(@Param("userId") Long userId);

}
