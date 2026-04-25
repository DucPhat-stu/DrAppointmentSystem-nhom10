CREATE EXTENSION IF NOT EXISTS btree_gist;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ex_time_slots_no_overlap'
    ) THEN
        ALTER TABLE time_slots
            ADD CONSTRAINT ex_time_slots_no_overlap
            EXCLUDE USING gist (
                schedule_id WITH =,
                tstzrange(start_time, end_time, '[)') WITH &&
            );
    END IF;
END $$;
