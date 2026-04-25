CREATE TABLE IF NOT EXISTS soap_notes (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL UNIQUE,
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    subjective TEXT NOT NULL,
    objective TEXT NOT NULL,
    assessment TEXT NOT NULL,
    plan TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_soap_notes_doctor_patient
    ON soap_notes (doctor_id, patient_id);

CREATE TABLE IF NOT EXISTS soap_note_audit_logs (
    id UUID PRIMARY KEY,
    soap_note_id UUID NOT NULL,
    appointment_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    action_name VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_soap_note_audit_appointment
    ON soap_note_audit_logs (appointment_id, created_at);
