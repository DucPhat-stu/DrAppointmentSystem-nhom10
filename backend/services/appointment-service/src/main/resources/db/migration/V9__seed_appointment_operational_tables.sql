-- Seed supporting operational tables so the demo database reflects the
-- same side effects produced by the application flow.
UPDATE appointments
SET reason = 'Follow-up for acute pharyngitis',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'a0000001-0000-0000-0000-000000000001';

UPDATE appointments
SET reason = 'Annual general checkup',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'a0000002-0000-0000-0000-000000000001';

INSERT INTO appointment_slot_sync_jobs (
    id,
    appointment_id,
    slot_id,
    operation,
    target_status,
    attempts,
    created_at,
    next_attempt_at,
    last_attempt_at,
    processed_at,
    last_error
)
VALUES
    (
        'b1000001-0000-0000-0000-000000000001',
        'a0000001-0000-0000-0000-000000000001',
        'e1000001-0000-0000-0000-000000000001',
        'RESERVE',
        'BOOKED',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'b1000002-0000-0000-0000-000000000001',
        'a0000002-0000-0000-0000-000000000001',
        'e1000002-0000-0000-0000-000000000002',
        'RESERVE',
        'BOOKED',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    )
ON CONFLICT DO NOTHING;

INSERT INTO appointment_event_outbox (
    id,
    event_name,
    appointment_id,
    doctor_id,
    patient_id,
    payload,
    attempts,
    created_at,
    last_attempt_at,
    published_at,
    last_error
)
VALUES
    (
        'b2000001-0000-0000-0000-000000000001',
        'APPOINTMENT_REQUESTED',
        'a0000001-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        '{"eventId":"b2000001-0000-0000-0000-000000000001","event":"APPOINTMENT_REQUESTED","appointmentId":"a0000001-0000-0000-0000-000000000001","doctorId":"22222222-2222-2222-2222-222222222222","patientId":"11111111-1111-1111-1111-111111111111"}',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'b2000002-0000-0000-0000-000000000001',
        'APPOINTMENT_CONFIRMED',
        'a0000001-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        '{"eventId":"b2000002-0000-0000-0000-000000000001","event":"APPOINTMENT_CONFIRMED","appointmentId":"a0000001-0000-0000-0000-000000000001","doctorId":"22222222-2222-2222-2222-222222222222","patientId":"11111111-1111-1111-1111-111111111111"}',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'b2000003-0000-0000-0000-000000000001',
        'APPOINTMENT_REQUESTED',
        'a0000002-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        '{"eventId":"b2000003-0000-0000-0000-000000000001","event":"APPOINTMENT_REQUESTED","appointmentId":"a0000002-0000-0000-0000-000000000001","doctorId":"22222222-2222-2222-2222-222222222222","patientId":"11111111-1111-1111-1111-111111111111"}',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    )
ON CONFLICT DO NOTHING;

INSERT INTO appointment_change_history (
    id,
    appointment_id,
    actor_id,
    actor_role,
    change_type,
    old_slot_id,
    new_slot_id,
    old_scheduled_start,
    old_scheduled_end,
    new_scheduled_start,
    new_scheduled_end,
    old_status,
    new_status,
    reason,
    created_at
)
VALUES
    (
        'b3000001-0000-0000-0000-000000000001',
        'a0000001-0000-0000-0000-000000000001',
        '11111111-1111-1111-1111-111111111111',
        'PATIENT',
        'CREATE',
        NULL,
        'e1000001-0000-0000-0000-000000000001',
        NULL,
        NULL,
        '2026-05-05T08:00:00+07:00',
        '2026-05-05T08:30:00+07:00',
        NULL,
        'PENDING',
        'Follow-up for acute pharyngitis',
        CURRENT_TIMESTAMP
    ),
    (
        'b3000002-0000-0000-0000-000000000001',
        'a0000001-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        'DOCTOR',
        'CONFIRM',
        'e1000001-0000-0000-0000-000000000001',
        'e1000001-0000-0000-0000-000000000001',
        '2026-05-05T08:00:00+07:00',
        '2026-05-05T08:30:00+07:00',
        '2026-05-05T08:00:00+07:00',
        '2026-05-05T08:30:00+07:00',
        'PENDING',
        'CONFIRMED',
        NULL,
        CURRENT_TIMESTAMP
    ),
    (
        'b3000003-0000-0000-0000-000000000001',
        'a0000002-0000-0000-0000-000000000001',
        '11111111-1111-1111-1111-111111111111',
        'PATIENT',
        'CREATE',
        NULL,
        'e1000002-0000-0000-0000-000000000002',
        NULL,
        NULL,
        '2026-05-06T08:30:00+07:00',
        '2026-05-06T09:00:00+07:00',
        NULL,
        'PENDING',
        'Annual general checkup',
        CURRENT_TIMESTAMP
    )
ON CONFLICT DO NOTHING;
