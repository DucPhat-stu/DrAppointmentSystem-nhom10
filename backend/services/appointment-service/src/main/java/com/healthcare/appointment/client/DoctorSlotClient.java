package com.healthcare.appointment.client;

import java.util.UUID;

public interface DoctorSlotClient {
    void updateSlotStatus(UUID slotId, String status);
}
