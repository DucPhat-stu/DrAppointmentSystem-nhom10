package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.TimeSlotEntity;
import com.healthcare.doctor.domain.TimeSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TimeSlotJpaRepository extends JpaRepository<TimeSlotEntity, UUID> {
    interface DoctorAvailabilityProjection {
        UUID getDoctorId();

        long getAvailableSlots();
    }

    List<TimeSlotEntity> findAllByScheduleIdOrderByStartTimeAsc(UUID scheduleId);

    boolean existsByScheduleIdAndStatus(UUID scheduleId, TimeSlotStatus status);

    @Query("""
            select slot
            from TimeSlotEntity slot
            where slot.status = :status
              and slot.scheduleId in (
                  select schedule.id
                  from DoctorScheduleEntity schedule
                  where schedule.doctorId = :doctorId
                    and schedule.date = :date
              )
            order by slot.startTime asc
            """)
    List<TimeSlotEntity> findAllByDoctorIdAndDateAndStatus(@Param("doctorId") UUID doctorId,
                                                           @Param("date") LocalDate date,
                                                           @Param("status") TimeSlotStatus status);

    @Query("""
            select schedule.doctorId as doctorId, count(slot.id) as availableSlots
            from TimeSlotEntity slot
            join DoctorScheduleEntity schedule on schedule.id = slot.scheduleId
            where schedule.date = :date
              and slot.status = :status
            group by schedule.doctorId
            order by schedule.doctorId asc
            """)
    List<DoctorAvailabilityProjection> findDoctorAvailabilityByDate(@Param("date") LocalDate date,
                                                                    @Param("status") TimeSlotStatus status);

    @Query("""
            select count(slot) > 0
            from TimeSlotEntity slot
            where slot.scheduleId = :scheduleId
              and slot.startTime < :newEnd
              and slot.endTime > :newStart
            """)
    boolean existsOverlap(@Param("scheduleId") UUID scheduleId,
                          @Param("newStart") OffsetDateTime newStart,
                          @Param("newEnd") OffsetDateTime newEnd);

    @Query("""
            select count(slot) > 0
            from TimeSlotEntity slot
            where slot.scheduleId = :scheduleId
              and slot.id <> :excludedId
              and slot.startTime < :newEnd
              and slot.endTime > :newStart
            """)
    boolean existsOverlapExcluding(@Param("scheduleId") UUID scheduleId,
                                   @Param("excludedId") UUID excludedId,
                                   @Param("newStart") OffsetDateTime newStart,
                                   @Param("newEnd") OffsetDateTime newEnd);

    @Query("""
            select count(slot) > 0
            from TimeSlotEntity slot
            where slot.status = :status
              and slot.scheduleId in (
                  select schedule.id
                  from DoctorScheduleEntity schedule
                  where schedule.doctorId = :doctorId
                    and schedule.date >= :startDate
                    and schedule.date < :endDate
              )
            """)
    boolean existsByDoctorDateRangeAndStatus(@Param("doctorId") UUID doctorId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("status") TimeSlotStatus status);

    @Modifying
    @Query("""
            update TimeSlotEntity slot
            set slot.status = :blockedStatus
            where slot.status = :availableStatus
              and slot.scheduleId in (
                  select schedule.id
                  from DoctorScheduleEntity schedule
                  where schedule.doctorId = :doctorId
                    and schedule.date >= :startDate
                    and schedule.date < :endDate
              )
            """)
    int blockAvailableSlotsForDoctorDateRange(@Param("doctorId") UUID doctorId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("availableStatus") TimeSlotStatus availableStatus,
                                              @Param("blockedStatus") TimeSlotStatus blockedStatus);
}
