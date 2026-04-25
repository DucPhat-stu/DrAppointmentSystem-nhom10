package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.TimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TimeSlotJpaRepository extends JpaRepository<TimeSlotEntity, UUID> {
    List<TimeSlotEntity> findAllByScheduleIdOrderByStartTimeAsc(UUID scheduleId);

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
}
