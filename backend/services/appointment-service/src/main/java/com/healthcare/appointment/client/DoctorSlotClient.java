package com.healthcare.appointment.client;

import com.healthcare.appointment.dto.DoctorSlotResponse;

import java.util.UUID;

public interface DoctorSlotClient {
    DoctorSlotResponse getSlot(UUID slotId);

    void updateSlotStatus(UUID slotId, String status);
}
