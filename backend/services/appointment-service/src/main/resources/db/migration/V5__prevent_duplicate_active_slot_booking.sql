CREATE UNIQUE INDEX IF NOT EXISTS uk_appointments_active_slot
    ON appointments (slot_id)
    WHERE slot_id IS NOT NULL
      AND status IN ('PENDING', 'CONFIRMED');
