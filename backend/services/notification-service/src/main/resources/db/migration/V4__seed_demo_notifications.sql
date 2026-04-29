-- Demo notifications for the seeded patient, doctor, and appointments.
INSERT INTO notifications (
    id,
    recipient_id,
    appointment_id,
    event_id,
    event_name,
    type,
    title,
    content,
    read_at,
    created_at
)
VALUES
    (
        'c1000001-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        'a0000001-0000-0000-0000-000000000001',
        'b2000001-0000-0000-0000-000000000001',
        'APPOINTMENT_REQUESTED',
        'APPOINTMENT',
        'New appointment request',
        'Patient Nguyen Van An requested an appointment on 2026-05-05 from 08:00 to 08:30.',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'c1000002-0000-0000-0000-000000000001',
        '11111111-1111-1111-1111-111111111111',
        'a0000001-0000-0000-0000-000000000001',
        'b2000002-0000-0000-0000-000000000001',
        'APPOINTMENT_CONFIRMED',
        'APPOINTMENT',
        'Appointment confirmed',
        'Dr. Tran Minh Duc confirmed your appointment on 2026-05-05 from 08:00 to 08:30.',
        NULL,
        CURRENT_TIMESTAMP
    ),
    (
        'c1000003-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        'a0000002-0000-0000-0000-000000000001',
        'b2000003-0000-0000-0000-000000000001',
        'APPOINTMENT_REQUESTED',
        'APPOINTMENT',
        'Pending appointment request',
        'Patient Nguyen Van An requested a general checkup on 2026-05-06 from 08:30 to 09:00.',
        NULL,
        CURRENT_TIMESTAMP
    ),
    (
        'c1000004-0000-0000-0000-000000000001',
        '11111111-1111-1111-1111-111111111111',
        NULL,
        NULL,
        'SYSTEM_REMINDER',
        'SYSTEM',
        'Remember your appointment',
        'You have a confirmed appointment with Dr. Tran Minh Duc on 2026-05-05 at 08:00.',
        NULL,
        CURRENT_TIMESTAMP
    )
ON CONFLICT DO NOTHING;
