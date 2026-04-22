INSERT INTO users (
    id,
    email,
    password_hash,
    role,
    status,
    full_name,
    phone,
    failed_login_attempts,
    created_at,
    updated_at
)
VALUES
    (
        '11111111-1111-1111-1111-111111111111',
        'patient01@healthcare.local',
        '$2a$12$qMgEvj19onicqj9FLpb8tujOwinOVmqKRHPHhAc6X3wmkqDde466K',
        'PATIENT',
        'ACTIVE',
        'Patient Seed',
        '0901000001',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '22222222-2222-2222-2222-222222222222',
        'doctor01@healthcare.local',
        '$2a$12$VcoAJmzSmeLBfq8SCPK/NeFW6BDKYrRXpVGFJgP9J3YAcqN4zWJai',
        'DOCTOR',
        'ACTIVE',
        'Doctor Seed',
        '0901000002',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (email) DO NOTHING;
