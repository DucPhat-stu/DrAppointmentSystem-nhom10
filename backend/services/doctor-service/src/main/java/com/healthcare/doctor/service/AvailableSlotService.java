package com.healthcare.doctor.service;

import com.healthcare.doctor.domain.TimeSlotStatus;
import com.healthcare.doctor.dto.AvailableSlotResponse;
import com.healthcare.doctor.entity.TimeSlotEntity;
import com.healthcare.doctor.repository.TimeSlotJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AvailableSlotService {
    private final TimeSlotJpaRepository timeSlotRepository;
    private final AvailableSlotCache availableSlotCache;

    public AvailableSlotService(TimeSlotJpaRepository timeSlotRepository,
                                AvailableSlotCache availableSlotCache) {
        this.timeSlotRepository = timeSlotRepository;
        this.availableSlotCache = availableSlotCache;
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> find(UUID doctorId, LocalDate date) {
        return availableSlotCache.get(doctorId, date)
                .orElseGet(() -> loadAndCache(doctorId, date));
    }

    private List<AvailableSlotResponse> loadAndCache(UUID doctorId, LocalDate date) {
        List<AvailableSlotResponse> slots = timeSlotRepository
                .findAllByDoctorIdAndDateAndStatus(doctorId, date, TimeSlotStatus.AVAILABLE)
                .stream()
                .map(slot -> toResponse(doctorId, slot))
                .toList();
        availableSlotCache.put(doctorId, date, slots);
        return slots;
    }

    private AvailableSlotResponse toResponse(UUID doctorId, TimeSlotEntity entity) {
        return new AvailableSlotResponse(
                entity.getId(),
                doctorId,
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus().name()
        );
    }
}
