CREATE TABLE IF NOT EXISTS appointment_change_history (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    actor_role VARCHAR(40) NOT NULL,
    change_type VARCHAR(40) NOT NULL,
    old_slot_id UUID,
    new_slot_id UUID,
    old_scheduled_start TIMESTAMP WITH TIME ZONE,
    old_scheduled_end TIMESTAMP WITH TIME ZONE,
    new_scheduled_start TIMESTAMP WITH TIME ZONE,
    new_scheduled_end TIMESTAMP WITH TIME ZONE,
    old_status VARCHAR(40),
    new_status VARCHAR(40),
    reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_appointment_change_history_appointment
    ON appointment_change_history (appointment_id, created_at DESC);
