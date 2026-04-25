package com.healthcare.doctor.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ScheduleResponse(UUID id, LocalDate date) {
}
