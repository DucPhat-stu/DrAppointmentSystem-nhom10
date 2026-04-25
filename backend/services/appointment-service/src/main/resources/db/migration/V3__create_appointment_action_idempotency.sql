CREATE TABLE IF NOT EXISTS appointment_action_idempotency (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    action_name VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    resulting_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_action_appointment
        FOREIGN KEY (appointment_id)
        REFERENCES appointments (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_appointment_action_idempotency UNIQUE (appointment_id, action_name, idempotency_key)
);
