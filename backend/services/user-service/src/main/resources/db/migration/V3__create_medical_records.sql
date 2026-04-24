-- Medical records (read-only for patients, created by doctors)
CREATE TABLE IF NOT EXISTS medical_records (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_code      VARCHAR(6)   NOT NULL UNIQUE,
    patient_id       UUID         NOT NULL,
    doctor_name      VARCHAR(255) NOT NULL,
    department       VARCHAR(100) NOT NULL,
    disease_summary  VARCHAR(500) NOT NULL,
    prescription     TEXT,
    visit_date       DATE         NOT NULL,
    appointment_date DATE,
    checkin_time     TIMESTAMPTZ,
    tests            TEXT,
    notes            TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medical_records_patient
        FOREIGN KEY (patient_id)
        REFERENCES user_profiles(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_medical_records_patient_id ON medical_records(patient_id);
CREATE INDEX IF NOT EXISTS idx_medical_records_visit_date ON medical_records(visit_date);
CREATE INDEX IF NOT EXISTS idx_medical_records_record_code ON medical_records(record_code);
