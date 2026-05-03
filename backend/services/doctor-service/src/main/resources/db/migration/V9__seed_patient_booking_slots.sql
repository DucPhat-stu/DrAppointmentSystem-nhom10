-- Extra available slots for the patient booking demo.
-- Doctor ID: 22222222-2222-2222-2222-222222222222 (doctor01)
-- Date matches the default demo date shown by the UI on 2026-05-01.

INSERT INTO doctor_schedules (id, doctor_id, schedule_date, created_at, updated_at)
VALUES ('d1000009-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '2026-05-01',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (doctor_id, schedule_date) DO NOTHING;

WITH schedule AS (
    SELECT id
    FROM doctor_schedules
    WHERE doctor_id = '22222222-2222-2222-2222-222222222222'
      AND schedule_date = '2026-05-01'
), slots(id, start_time, end_time) AS (
    VALUES
        ('e1000009-0000-0000-0000-000000000001', '2026-05-01T08:00:00+07:00', '2026-05-01T08:30:00+07:00'),
        ('e1000009-0000-0000-0000-000000000002', '2026-05-01T08:30:00+07:00', '2026-05-01T09:00:00+07:00'),
        ('e1000009-0000-0000-0000-000000000003', '2026-05-01T09:00:00+07:00', '2026-05-01T09:30:00+07:00'),
        ('e1000009-0000-0000-0000-000000000004', '2026-05-01T09:30:00+07:00', '2026-05-01T10:00:00+07:00'),
        ('e1000009-0000-0000-0000-000000000005', '2026-05-01T10:30:00+07:00', '2026-05-01T11:00:00+07:00'),
        ('e1000009-0000-0000-0000-000000000006', '2026-05-01T11:00:00+07:00', '2026-05-01T11:30:00+07:00'),
        ('e1000009-0000-0000-0000-000000000007', '2026-05-01T13:30:00+07:00', '2026-05-01T14:00:00+07:00'),
        ('e1000009-0000-0000-0000-000000000008', '2026-05-01T14:00:00+07:00', '2026-05-01T14:30:00+07:00'),
        ('e1000009-0000-0000-0000-000000000009', '2026-05-01T14:30:00+07:00', '2026-05-01T15:00:00+07:00'),
        ('e1000009-0000-0000-0000-000000000010', '2026-05-01T15:00:00+07:00', '2026-05-01T15:30:00+07:00'),
        ('e1000009-0000-0000-0000-000000000011', '2026-05-01T15:30:00+07:00', '2026-05-01T16:00:00+07:00'),
        ('e1000009-0000-0000-0000-000000000012', '2026-05-01T16:00:00+07:00', '2026-05-01T16:30:00+07:00')
)
INSERT INTO time_slots (id, schedule_id, start_time, end_time, status, created_at, updated_at)
SELECT slots.id::uuid,
       schedule.id,
       slots.start_time::timestamp with time zone,
       slots.end_time::timestamp with time zone,
       'AVAILABLE',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM schedule
CROSS JOIN slots
ON CONFLICT DO NOTHING;
