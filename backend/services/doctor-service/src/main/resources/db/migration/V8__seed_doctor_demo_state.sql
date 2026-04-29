-- Align doctor-service slot state with appointment-service demo appointments.
UPDATE time_slots
SET status = 'BOOKED',
    updated_at = CURRENT_TIMESTAMP
WHERE id IN (
    'e1000001-0000-0000-0000-000000000001',
    'e1000002-0000-0000-0000-000000000002'
);

INSERT INTO doctor_leaves (
    id,
    doctor_id,
    start_date,
    end_date,
    status,
    rejection_reason,
    decided_by,
    decided_at,
    created_at,
    updated_at
)
VALUES
    (
        'f1000001-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '2026-05-12',
        '2026-05-13',
        'PENDING',
        NULL,
        NULL,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'f1000002-0000-0000-0000-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '2026-05-19',
        '2026-05-20',
        'APPROVED',
        NULL,
        '22222222-2222-2222-2222-222222222222',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT DO NOTHING;

INSERT INTO soap_notes (
    id,
    appointment_id,
    doctor_id,
    patient_id,
    subjective,
    objective,
    assessment,
    plan,
    created_at,
    updated_at
)
VALUES (
    'f2000001-0000-0000-0000-000000000001',
    'a0000001-0000-0000-0000-000000000001',
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'Patient reports mild sore throat and dry cough improving after medication.',
    'No fever. Throat mildly erythematous. Lungs clear.',
    'Improving acute pharyngitis.',
    'Continue hydration, finish medication course, return if fever or breathing symptoms occur.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT DO NOTHING;

INSERT INTO soap_note_audit_logs (
    id,
    soap_note_id,
    appointment_id,
    doctor_id,
    action_name,
    created_at
)
VALUES (
    'f3000001-0000-0000-0000-000000000001',
    'f2000001-0000-0000-0000-000000000001',
    'a0000001-0000-0000-0000-000000000001',
    '22222222-2222-2222-2222-222222222222',
    'CREATE',
    CURRENT_TIMESTAMP
)
ON CONFLICT DO NOTHING;
