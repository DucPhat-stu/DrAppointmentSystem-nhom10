CREATE TABLE IF NOT EXISTS doctor_schedules (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL,
    schedule_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_doctor_schedules_doctor_date UNIQUE (doctor_id, schedule_date)
);

CREATE INDEX IF NOT EXISTS idx_doctor_schedules_doctor_date
    ON doctor_schedules (doctor_id, schedule_date);
