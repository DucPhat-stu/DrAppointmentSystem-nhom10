CREATE TABLE IF NOT EXISTS time_slots (
    id UUID PRIMARY KEY,
    schedule_id UUID NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_time_slots_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES doctor_schedules (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_time_slots_time_range CHECK (start_time < end_time),
    CONSTRAINT ck_time_slots_status CHECK (status IN ('AVAILABLE', 'BOOKED', 'BLOCKED'))
);

CREATE INDEX IF NOT EXISTS idx_time_slots_schedule_start
    ON time_slots (schedule_id, start_time);
