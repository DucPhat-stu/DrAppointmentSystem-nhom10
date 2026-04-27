package com.healthcare.doctor.service;

import com.healthcare.doctor.domain.TimeSlotStatus;
import com.healthcare.doctor.dto.DoctorAvailabilityResponse;
import com.healthcare.doctor.repository.TimeSlotJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DoctorDirectoryService {
    private final TimeSlotJpaRepository timeSlotRepository;

    public DoctorDirectoryService(TimeSlotJpaRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> findAvailableDoctors(LocalDate date) {
        return timeSlotRepository.findDoctorAvailabilityByDate(date, TimeSlotStatus.AVAILABLE)
                .stream()
                .map(item -> new DoctorAvailabilityResponse(item.getDoctorId(), date, item.getAvailableSlots()))
                .toList();
    }
}
