-- Keep demo data readable and stable even if earlier seed files were applied
-- with a different console/file encoding.
UPDATE user_profiles
SET full_name = 'Nguyen Van An',
    address = '123 Nguyen Hue, District 1, Ho Chi Minh City',
    emergency_contact = '0901999999',
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = '11111111-1111-1111-1111-111111111111';

UPDATE user_profiles
SET full_name = 'Dr. Tran Minh Duc',
    address = '456 Le Loi, District 3, Ho Chi Minh City',
    specialty = COALESCE(specialty, 'Otolaryngology'),
    department = COALESCE(department, 'ENT'),
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = '22222222-2222-2222-2222-222222222222';

UPDATE medical_records
SET doctor_name = 'Dr. Tran Minh Duc',
    department = 'ENT',
    disease_summary = 'Acute pharyngitis',
    prescription = 'Amoxicillin 500mg three times daily for 7 days; Paracetamol 500mg when fever occurs',
    tests = '["Complete blood count", "Throat endoscopy"]',
    notes = 'Follow up after 7 days. Avoid cold drinks.'
WHERE record_code = '000001';

UPDATE medical_records
SET doctor_name = 'Dr. Le Thi Huong',
    department = 'Internal Medicine',
    disease_summary = 'Chronic gastritis',
    prescription = 'Omeprazole 20mg twice daily before meals; Sucralfate 1g four times daily',
    tests = '["H. pylori test", "Gastroscopy"]',
    notes = 'Avoid spicy food and alcohol. Follow up after 4 weeks.'
WHERE record_code = '000002';

UPDATE medical_records
SET doctor_name = 'Dr. Nguyen Thanh Son',
    department = 'Cardiology',
    disease_summary = 'Stage 1 hypertension',
    prescription = 'Amlodipine 5mg every morning; Aspirin 81mg daily',
    tests = '["ECG", "Blood lipid panel", "24-hour blood pressure monitoring"]',
    notes = 'Monitor blood pressure at home daily. Reduce salt intake and exercise regularly.'
WHERE record_code = '000003';

UPDATE medical_records
SET doctor_name = 'Dr. Pham Quoc Bao',
    department = 'Dermatology',
    disease_summary = 'Contact dermatitis',
    prescription = 'Hydrocortisone cream 1% twice daily; Cetirizine 10mg every evening',
    tests = '["Allergy test"]',
    notes = 'Avoid chemical exposure. Use fragrance-free moisturizer.'
WHERE record_code = '000004';
