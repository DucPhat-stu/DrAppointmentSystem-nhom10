-- Seed profile for patient01 (matches auth-service user)
INSERT INTO user_profiles (id, user_id, full_name, email, phone, address, date_of_birth, gender, emergency_contact)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '11111111-1111-1111-1111-111111111111',
    'Nguyễn Văn An',
    'patient01@healthcare.local',
    '0901000001',
    '123 Nguyễn Huệ, Quận 1, TP.HCM',
    '1990-05-15',
    'MALE',
    '0901999999'
) ON CONFLICT (user_id) DO NOTHING;

-- Seed profile for doctor01
INSERT INTO user_profiles (id, user_id, full_name, email, phone, address, date_of_birth, gender)
VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '22222222-2222-2222-2222-222222222222',
    'TS.BS Trần Minh Đức',
    'doctor01@healthcare.local',
    '0901000002',
    '456 Lê Lợi, Quận 3, TP.HCM',
    '1978-11-20',
    'MALE'
) ON CONFLICT (user_id) DO NOTHING;

-- Seed medical records for patient01
INSERT INTO medical_records (id, record_code, patient_id, doctor_name, department, disease_summary, prescription, visit_date, appointment_date, checkin_time, tests, notes)
VALUES
(
    'cccccccc-cccc-cccc-cccc-cccccccccc01',
    '000001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'TS.BS Trần Minh Đức',
    'Tai Mũi Họng',
    'Viêm họng cấp',
    'Amoxicillin 500mg x 3 lần/ngày x 7 ngày, Paracetamol 500mg khi sốt',
    '2026-04-01',
    '2026-03-28',
    '2026-04-01T08:30:00+07:00',
    '["Xét nghiệm máu tổng quát", "Nội soi họng"]',
    'Bệnh nhân cần tái khám sau 7 ngày. Tránh đồ lạnh.'
),
(
    'cccccccc-cccc-cccc-cccc-cccccccccc02',
    '000002',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'BS. Lê Thị Hương',
    'Nội Khoa',
    'Viêm dạ dày mãn tính',
    'Omeprazole 20mg x 2 lần/ngày trước ăn, Sucralfate 1g x 4 lần/ngày',
    '2026-03-15',
    '2026-03-12',
    '2026-03-15T09:15:00+07:00',
    '["Xét nghiệm H.Pylori", "Nội soi dạ dày"]',
    'Kiêng cay nóng, rượu bia. Tái khám sau 4 tuần.'
),
(
    'cccccccc-cccc-cccc-cccc-cccccccccc03',
    '000003',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'PGS.TS Nguyễn Thanh Sơn',
    'Tim Mạch',
    'Tăng huyết áp giai đoạn 1',
    'Amlodipine 5mg x 1 lần/ngày sáng, Aspirin 81mg x 1 lần/ngày',
    '2026-02-20',
    '2026-02-18',
    '2026-02-20T10:00:00+07:00',
    '["Điện tâm đồ (ECG)", "Xét nghiệm lipid máu", "Đo huyết áp 24h"]',
    'Theo dõi huyết áp tại nhà hàng ngày. Giảm muối, tập thể dục.'
),
(
    'cccccccc-cccc-cccc-cccc-cccccccccc04',
    '000004',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'BS. Phạm Quốc Bảo',
    'Da Liễu',
    'Viêm da tiếp xúc',
    'Hydrocortisone cream 1% bôi 2 lần/ngày, Cetirizine 10mg 1 viên/tối',
    '2026-01-10',
    '2026-01-08',
    '2026-01-10T14:00:00+07:00',
    '["Xét nghiệm dị ứng"]',
    'Tránh tiếp xúc hoá chất. Dùng kem dưỡng ẩm không mùi.'
)
ON CONFLICT (record_code) DO NOTHING;
