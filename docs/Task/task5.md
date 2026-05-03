🧩 EPIC: Admin Management
🔹 Story A1 — Admin Authentication
BE Task: Add ADMIN Role

Scope

thêm role ADMIN vào hệ thống

Acceptance Criteria

 JWT chứa role ADMIN
 ADMIN login được
 RBAC phân biệt ADMIN / DOCTOR / PATIENT



🔹 Story A2 — User Management
BE Task: Admin Get Users

API

GET /admin/users

Acceptance Criteria

 trả list users
 pagination
 chỉ ADMIN gọi được
BE Task: Disable User
PUT /admin/users/{id}/disable

Acceptance Criteria

 user bị disable không login được
 status update DB



FE Task: Admin User Page

Scope

table user
button disable



🔹 Story A3 — Doctor Management
BE Task: Approve Doctor
PUT /admin/doctors/{id}/approve

Acceptance Criteria

 doctor status ACTIVE
 doctor mới được tạo slot
BE Task: Disable Doctor
PUT /admin/doctors/{id}/disable
FE Task: Admin Doctor Page
list doctor
approve / disable



🔹 Story A4 — Appointment Governance
BE Task: Admin View All Appointments
GET /admin/appointments

Acceptance Criteria

 filter theo status
 pagination
BE Task: Force Cancel Appointment
PUT /admin/appointments/{id}/cancel

Acceptance Criteria

 slot được release
 notification gửi đi
FE Task: Admin Appointment Dashboard
table + filter
cancel button



🔹 Story A5 — Basic Audit Log
BE Task: Log Admin Actions

Scope

log:
disable user
cancel appointment

Acceptance Criteria

 lưu DB hoặc log file
 có timestamp + adminId



5. Scope tối thiểu (nếu bạn chỉ có thời gian rất ít)

👉 Chỉ cần làm:

MUST
ADMIN role
GET /admin/users
GET /admin/appointments
SHOULD
disable user
force cancel appointment
NICE
UI admin
6. Risk khi thêm Admin (cần note trong báo cáo)
🔴 High
privilege escalation (user giả admin)
thiếu RBAC check
🟡 Medium
admin thao tác sai → mất data
force cancel gây inconsistency
🟢 Low
UI admin chưa đẹp