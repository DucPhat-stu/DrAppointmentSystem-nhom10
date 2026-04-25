EPIC 1: Doctor Schedule Management
🎯 1.1 Domain Definition
Entities

doctor_schedules

id (UUID)
doctor_id (UUID)
date (DATE)
created_at
updated_at

time_slots

id (UUID)
schedule_id (FK)
start_time (TIMESTAMP)
end_time (TIMESTAMP)
status (ENUM: AVAILABLE, BOOKED, BLOCKED)
⚙️ 1.2 Business Rules (BẮT BUỘC)
Một schedule = 1 ngày làm việc của doctor
Một schedule có nhiều time_slots
Không được:
Tạo 2 schedule cùng ngày cho 1 doctor
Tạo slot overlap (start < end của slot khác)
Slot mặc định = AVAILABLE
Slot BOOKED:
Không được sửa
Không được xóa
🔌 1.3 API Contract (CHUẨN)
POST /api/v1/doctors/schedules
{
  "date": "2026-06-25"
}

Response:

{
  "id": "uuid",
  "date": "2026-06-25"
}
POST /api/v1/doctors/time-slots
{
  "scheduleId": "uuid",
  "startTime": "2026-06-25T08:00:00",
  "endTime": "2026-06-25T09:00:00"
}
🔒 1.4 Validation Rules
doctorId lấy từ JWT → KHÔNG nhận từ request
startTime < endTime
Slot phải thuộc schedule của doctor
Check overlap bằng query:
WHERE schedule_id = ?
AND start_time < :newEnd
AND end_time > :newStart
⚠️ 1.5 Critical Notes
Timezone: dùng UTC toàn hệ thống
Không dùng LocalDateTime nếu chưa config timezone
Transaction bắt buộc khi tạo nhiều slot
Index cần có:
(schedule_id, start_time)
🧩 EPIC 2: Appointment Management
🎯 2.1 Domain Boundary
❌ doctor-service KHÔNG lưu appointment
✅ Chỉ call:
appointment-service
notification-service
🔄 2.2 Data Flow
Doctor → doctor-service → appointment-service
                         ↓
                   notification-service
⚙️ 2.3 Business Rules
Doctor chỉ thấy appointment của mình
State machine:
PENDING → CONFIRMED → COMPLETED
        → CANCELLED
🔌 2.4 API Contract
GET /api/v1/doctors/appointments

Query:

?date=2026-06-25&status=PENDING
PUT /appointments/{id}/confirm

Response:

{
  "status": "CONFIRMED"
}
🔁 2.5 Idempotency (RẤT QUAN TRỌNG)
Nếu gọi confirm 2 lần:
Không lỗi
Không duplicate event
Cách làm:
Check state trước khi update
Hoặc dùng idempotency_key
📡 2.6 Event Publishing

Khi confirm:

{
  "event": "APPOINTMENT_CONFIRMED",
  "appointmentId": "...",
  "doctorId": "...",
  "patientId": "..."
}
⚠️ 2.7 Critical Notes
Timeout call service khác: max 2s
Retry: 3 lần (exponential backoff)
Không block request khi gửi event
🧩 EPIC 3: SOAP Note & Patient History
🎯 3.1 SOAP Note Structure
{
  "subjective": "...",
  "objective": "...",
  "assessment": "...",
  "plan": "..."
}
⚙️ 3.2 Business Rules
Chỉ doctor của appointment được ghi
1 appointment = 1 SOAP note
Có thể update
🔒 3.3 Security
Validate:
appointment.doctor_id == currentUserId
🧼 3.4 Data Sanitization
Không lưu raw HTML nếu chưa sanitize
Chống XSS:
strip script tag
escape HTML
🔄 3.5 Patient History Aggregation
Sources:
appointment-service
medical-record-service
Response mẫu:
[
  {
    "appointmentId": "...",
    "date": "...",
    "diagnosis": "...",
    "doctorName": "...",
    "notes": "..."
  }
]
⚠️ 3.6 Critical Notes
Không join DB trực tiếp → luôn qua API
Cache optional (Redis)
🧩 EPIC 4: Leave Management
🎯 4.1 Domain

doctor_leaves

id
doctor_id
start_date
end_date
status (PENDING, APPROVED, REJECTED)
⚙️ 4.2 Business Rules
Doctor tạo request → status = PENDING
Admin:
approve → APPROVED
reject → REJECTED
🔒 4.3 Validation
start_date < end_date
Không overlap với leave khác
⚠️ 4.4 Critical Notes
Khi APPROVED:
cần BLOCK slot tương ứng
(có thể trigger async job)
🧩 EPIC 5: Available Slots (Patient View)
🎯 5.1 Logic
Chỉ trả slot:
status = AVAILABLE
⚙️ 5.2 Performance
Dùng Redis cache:
key: doctor:{id}:slots:{date}
TTL: 5–10 phút
🔄 5.3 Flow
FE → doctor-service → DB/Cache → response
⚠️ 5.4 Critical Notes
Cache invalidation khi:
slot update
appointment booked
🧩 EPIC 6: Cross-cutting
🔐 6.1 Authentication
Header:
Authorization: Bearer <token>
⚙️ 6.2 RBAC
Role	Access
DOCTOR	full doctor APIs
ADMIN	leave approval
PATIENT	view slots
🧾 6.3 Standard Response Format
{
  "success": true,
  "data": {},
  "error": null
}
🧨 6.4 Error Handling
Case	Code
Unauthorized	401
Forbidden	403
Not Found	404
Conflict	409
📊 6.5 Logging
Mỗi request có:
traceId
userId
timestamp
⚠️ 6.6 Critical Notes
Không trust FE input
Validate toàn bộ ở BE
Không expose internal ID nếu không cần
🚨 GLOBAL ANTI-MISTAKE RULES (QUAN TRỌNG NHẤT)
❌ Không lưu dữ liệu của service khác
❌ Không hardcode role/user
❌ Không bỏ qua validate ownership
❌ Không xử lý business logic ở FE
✅ Luôn dùng transaction khi cần
✅ Luôn check state trước update
✅ Luôn thiết kế API idempotent