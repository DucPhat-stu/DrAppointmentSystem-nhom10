CREATE TABLE IF NOT EXISTS appointments (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    slot_id UUID,
    scheduled_start TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(500),
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_appointments_time_range CHECK (scheduled_start < scheduled_end),
    CONSTRAINT ck_appointments_status CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED', 'RESCHEDULED'))
);

CREATE INDEX IF NOT EXISTS idx_appointments_doctor_start
    ON appointments (doctor_id, scheduled_start);

CREATE INDEX IF NOT EXISTS idx_appointments_doctor_status
    ON appointments (doctor_id, status);
