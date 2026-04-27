ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS specialty VARCHAR(120),
    ADD COLUMN IF NOT EXISTS department VARCHAR(120);

UPDATE user_profiles
SET specialty = COALESCE(specialty, 'Otolaryngology'),
    department = COALESCE(department, 'ENT')
WHERE user_id = '22222222-2222-2222-2222-222222222222';
