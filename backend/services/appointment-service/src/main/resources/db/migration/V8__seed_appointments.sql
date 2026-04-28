-- Seed 2 sample appointments for patient01 with doctor01
-- These appointments reference slots from doctor-service seed (V7)
-- but appointment-service stores scheduled_start/end directly so no FK dependency.
--
-- patient01: 11111111-1111-1111-1111-111111111111
-- doctor01:  22222222-2222-2222-2222-222222222222
-- Slot IDs from doctor-service V7 seed (same UUIDs for clarity; status managed independently)

INSERT INTO appointments (
    id, doctor_id, patient_id, slot_id,
    scheduled_start, scheduled_end,
    status, reason,
    created_at, updated_at
)
VALUES
    -- Appointment 1: CONFIRMED – doctor has already accepted
    (
        'a0000001-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        'e1000001-0000-0000-0000-000000000001',
        '2026-05-05T08:00:00+07:00',
        '2026-05-05T08:30:00+07:00',
        'CONFIRMED',
        'Tái khám viêm họng cấp',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- Appointment 2: PENDING – waiting for doctor confirmation
    (
        'a0000002-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        'e1000002-0000-0000-0000-000000000002',
        '2026-05-06T08:30:00+07:00',
        '2026-05-06T09:00:00+07:00',
        'PENDING',
        'Khám tổng quát định kỳ',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT DO NOTHING;
