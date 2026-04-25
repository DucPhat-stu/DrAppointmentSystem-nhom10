CREATE TABLE IF NOT EXISTS appointment_event_outbox (
    id UUID PRIMARY KEY,
    event_name VARCHAR(128) NOT NULL,
    appointment_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    payload TEXT NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_appointment_event_outbox_pending
    ON appointment_event_outbox (published_at, attempts, created_at);
