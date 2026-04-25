CREATE TABLE IF NOT EXISTS doctor_leaves (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    rejection_reason VARCHAR(500),
    decided_by UUID,
    decided_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_doctor_leaves_date_range CHECK (start_date < end_date),
    CONSTRAINT ck_doctor_leaves_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE EXTENSION IF NOT EXISTS btree_gist;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ex_doctor_leaves_no_active_overlap'
    ) THEN
        ALTER TABLE doctor_leaves
            ADD CONSTRAINT ex_doctor_leaves_no_active_overlap
            EXCLUDE USING gist (
                doctor_id WITH =,
                daterange(start_date, end_date, '[)') WITH &&
            )
            WHERE (status IN ('PENDING', 'APPROVED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_doctor_leaves_doctor_dates
    ON doctor_leaves (doctor_id, start_date, end_date);

CREATE INDEX IF NOT EXISTS idx_doctor_leaves_status_created
    ON doctor_leaves (status, created_at DESC);
