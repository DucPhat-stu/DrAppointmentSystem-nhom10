CREATE TABLE IF NOT EXISTS appointment_slot_sync_jobs (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL,
    slot_id UUID NOT NULL,
    operation VARCHAR(40) NOT NULL,
    target_status VARCHAR(40) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_appointment_slot_sync_jobs_pending
    ON appointment_slot_sync_jobs (processed_at, attempts, next_attempt_at, created_at);
