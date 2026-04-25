package com.healthcare.doctor.repository;

import com.healthcare.doctor.domain.LeaveStatus;
import com.healthcare.doctor.entity.DoctorLeaveEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorLeaveJpaRepository
        extends JpaRepository<DoctorLeaveEntity, UUID>, JpaSpecificationExecutor<DoctorLeaveEntity> {
    List<DoctorLeaveEntity> findAllByDoctorIdOrderByStartDateDesc(UUID doctorId);

    Optional<DoctorLeaveEntity> findByIdAndDoctorId(UUID id, UUID doctorId);

    @Query("""
            select count(leave) > 0
            from DoctorLeaveEntity leave
            where leave.doctorId = :doctorId
              and leave.status in :statuses
              and leave.startDate < :endDate
              and leave.endDate > :startDate
            """)
    boolean existsActiveOverlap(@Param("doctorId") UUID doctorId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                @Param("statuses") List<LeaveStatus> statuses);

    static Specification<DoctorLeaveEntity> statusIs(LeaveStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }
}
