⚠️ QUY TẮC TRIỂN KHAI (BẮT BUỘC)
❌ Không lưu data của service khác
✅ Luôn validate ownership từ JWT
✅ API phải idempotent
✅ Event-driven cho notification
✅ Timeout external call < 2s

🧩 EPIC 1: Doctor Schedule Management

Goal: Bác sĩ quản lý lịch làm việc và khung giờ khám

📘 STORY 1.1: Manage Working Schedule

As a Doctor
I want tạo và quản lý lịch làm việc
So that tôi có thể định nghĩa thời gian khám bệnh

🎯 Acceptance Criteria
Doctor chỉ thao tác trên dữ liệu của mình
Không tạo schedule trùng thời gian
API trả đúng format chuẩn
🔧 TASK 1.1.1 [BE] Schedule API Development
Subtasks:
Create migration doctor_schedules
Define entity + repository
Implement API:
POST /doctors/schedules
GET /doctors/schedules
PUT /doctors/schedules/{id}
DELETE /doctors/schedules/{id}
Validate time overlap
Extract doctorId từ JWT
Unit test service layer
🎨 TASK 1.1.2 [FE] Schedule UI
Subtasks:
Build calendar component (week view)
Form create/update schedule
API integration (CRUD)
State management (loading/error)
Re-render UI after update
📘 STORY 1.2: Manage Time Slots

As a Doctor
I want tạo khung giờ khám
So that bệnh nhân có thể đặt lịch

🎯 Acceptance Criteria
Slot thuộc schedule hợp lệ
Không overlap slot
Không xóa slot đã BOOKED
🔧 TASK 1.2.1 [BE] Time Slot APIs
Subtasks:
Create migration time_slots
Define enum TimeSlotStatus
Implement:
POST /time-slots
PUT /time-slots/{id}
DELETE /time-slots/{id}
Validate slot thuộc schedule
Check overlap slot
Transaction khi tạo nhiều slot
🎨 TASK 1.2.2 [FE] Time Slot UI
Subtasks:
UI thêm slot (time picker)
Hiển thị danh sách slot
Hiển thị trạng thái (AVAILABLE, BOOKED)
Disable edit/delete khi BOOKED
🧩 EPIC 2: Appointment Management (Doctor Side)
📘 STORY 2.1: View Doctor Appointments

As a Doctor
I want xem danh sách lịch hẹn
So that tôi quản lý công việc

🎯 AC
Chỉ thấy appointment của mình
Có filter theo ngày và trạng thái
Có pagination
🔧 TASK 2.1.1 [BE] Appointment Query
Subtasks:
Implement API GET /appointments
Implement API GET /appointments/{id}
Call appointment-service (REST/gRPC)
Mapping response DTO
Add filter (date, status)
Handle timeout + fallback
🎨 TASK 2.1.2 [FE] Appointment Dashboard
Subtasks:
Build list UI
Filter UI (date/status)
Detail view
Call API + render data
📘 STORY 2.2: Manage Appointment Actions

As a Doctor
I want confirm/reject/cancel lịch hẹn
So that kiểm soát quá trình khám

🎯 AC
Validate state transition
Không double request
Gửi event sang notification-service
🔧 TASK 2.2.1 [BE] Appointment Actions
Subtasks:
API PUT /appointments/{id}/confirm
API PUT /appointments/{id}/reject
API PUT /appointments/{id}/cancel
Validate trạng thái:
PENDING → CONFIRMED
CONFIRMED → CANCELLED
Publish event (Kafka/RabbitMQ)
Implement idempotency key
🎨 TASK 2.2.2 [FE] Appointment Actions UI
Subtasks:
Button Confirm/Reject/Cancel
Confirm modal
Disable button khi loading
Optimistic UI update
🧩 EPIC 3: Medical Notes & History
📘 STORY 3.1: SOAP Note Management

As a Doctor
I want ghi chú sau khám
So that lưu thông tin điều trị

🎯 AC
Chỉ doctor của appointment được ghi
Structure đúng SOAP
🔧 TASK 3.1.1 [BE] SOAP Note API
Subtasks:
Create migration soap_notes
API POST /appointments/{id}/soap
API GET /appointments/{id}/soap
Validate doctor ownership
Sanitize input
Audit log
🎨 TASK 3.1.2 [FE] SOAP Note UI
Subtasks:
Build form nhập SOAP
Call API save/update
View note
Validate input
📘 STORY 3.2: Patient History

As a Doctor
I want xem lịch sử bệnh nhân
So that hiểu tình trạng bệnh

🔧 TASK 3.2.1 [BE] Patient History API
Subtasks:
API GET /patients/{id}/history
Aggregate:
appointment-service
medical-record-service
Pagination
Mapping unified response
🎨 TASK 3.2.2 [FE] History UI
Subtasks:
Timeline UI
Detail modal/page
Lazy loading
🧩 EPIC 4: Leave Management
📘 STORY 4.1: Doctor Leave Request

As a Doctor
I want gửi đơn xin nghỉ
So that không bị đặt lịch

🔧 TASK 4.1.1 [BE]
Subtasks:
Migration doctor_leaves
API POST /leaves
Validate date range
🎨 TASK 4.1.2 [FE]
Subtasks:
Form submit leave request
Validate input
Call API
📘 STORY 4.2: Admin Approve Leave
🔧 TASK 4.2.1 [BE]
Subtasks:
API GET /admin/leaves
API PUT /admin/leaves/{id}/approve
API PUT /admin/leaves/{id}/reject
RBAC ADMIN
🎨 TASK 4.2.2 [FE]
Subtasks:
Admin dashboard
Approve/Reject buttons
🧩 EPIC 5: Patient View Available Slots
📘 STORY 5.1: View Available Slots
🔧 TASK 5.1.1 [BE]
Subtasks:
API GET /doctors/{id}/available-slots
Filter status AVAILABLE
Redis cache (TTL 5–10 phút)
🎨 TASK 5.1.2 [FE]
Subtasks:
Calendar picker
Slot list UI
Select slot
🧩 EPIC 6: Cross-cutting Infrastructure
📘 STORY 6.1: Security & Auth
🔧 TASK 6.1.1 [BE]
Subtasks:
JWT filter
RBAC middleware
Extract user from token
🎨 TASK 6.1.2 [FE]
Subtasks:
Axios interceptor
Attach token
Handle 401
📘 STORY 6.2: System Quality
🔧 TASK 6.2.1 [BE]
Subtasks:
Global exception handler
Logging (traceId)
Swagger docs
🎨 TASK 6.2.2 [FE]
Subtasks:
Loading state
Error handling
UX fallback