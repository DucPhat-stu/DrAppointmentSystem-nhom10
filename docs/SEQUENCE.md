



HEALTHCARE PLATFORM
Sequence Diagram Specification
Tài liệu Thiết kế Kỹ thuật — Technical Design Document



Phiên bản	2.0 — Sequence Diagram Release
Kiến trúc	Microservices (12 Services)
Tổng Sequence Diagrams	13 Service SD + 1 System Overview SD
Chuẩn ký hiệu	PlantUML / UML 2.5
Năm	2026
Phân loại	Tài liệu Kỹ thuật Nội bộ — Confidential
 
MỤC LỤC
#	Nội dung	Trang
1	Tổng quan tài liệu & Quy ước ký hiệu	3
2	SD-00 — System Overview Sequence Diagram	5
3	SD-01 — auth-service: Xác thực & Phân quyền	8
4	SD-02 — user-service: Quản lý Hồ sơ	10
5	SD-03 — doctor-service: Lịch làm việc & Khám bệnh	12
6	SD-04 — appointment-service: Đặt lịch & Vòng đời	14
7	SD-05 — notification-service: Đa kênh Thông báo	16
8	SD-06 — medical-record-service: Hồ sơ Bệnh án	18
9	SD-07 — ai-service: AI Chatbot & Chẩn đoán	20
10	SD-08 — payment-service: Thanh toán & Hoàn tiền	22
11	SD-09 — prescription-service: Đơn thuốc Điện tử	24
12	SD-10 — review-service: Đánh giá Bác sĩ	26
13	SD-11 — report-service: Báo cáo & BI	27
14	SD-12 — admin-service: Quản trị Hệ thống	28
15	Ma trận Phụ thuộc Service	30
16	Error Handling & Non-Happy Path	31
 
1. Tổng Quan Tài Liệu & Quy Ước Ký Hiệu
1.1 Mục đích
Tài liệu này định nghĩa đầy đủ các Sequence Diagram (Biểu đồ Tuần tự) cho Healthcare Platform — hệ thống quản lý y tế trực tuyến được xây dựng theo kiến trúc Microservices. Mỗi diagram mô tả chi tiết luồng tương tác giữa các actor, service, và hệ thống ngoài trong từng use case tiêu biểu.

1.2 Phạm vi
Tài liệu bao gồm 14 Sequence Diagram chính:
•	01 System Overview SD — Luồng tổng quát toàn hệ thống (end-to-end patient journey)
•	12 Service-level SD — Mỗi microservice có ít nhất 1 diagram chính cho happy path
•	01 AI Service SD — Được tách riêng do độ phức tạp cao (NLP + Computer Vision)
•	Phần bổ sung: Error Handling, Security Flow, và Ma trận phụ thuộc service

1.3 Quy Ước Ký Hiệu UML 2.5
Ký hiệu	Cú pháp PlantUML	Ý nghĩa
Synchronous Call	A -> B : msg	Gọi đồng bộ — caller chờ response
Asynchronous Msg	A ->> B : msg	Gửi bất đồng bộ — không chờ (event/queue)
Return/Response	B --> A : result	Trả về kết quả (mũi tên đứt nét)
Self-call	A -> A : validate()	Gọi nội bộ trong cùng service
alt / else	alt [condition]\nelse\nend	Phân nhánh điều kiện (if/else)
loop	loop [condition]	Vòng lặp (retry, pagination)
opt	opt [condition]	Bước tuỳ chọn (may or may not happen)
group	group Label	Nhóm các bước liên quan
note	note over A : text	Chú thích trên participant
Database	database DB	Kho lưu trữ (PostgreSQL, Redis...)
Queue	queue MQ	Message broker (RabbitMQ, Kafka)
Activate	activate / deactivate	Hiển thị lifecycle của object

1.4 Participant Legend
Loại	Ký hiệu	Ví dụ trong hệ thống
Actor người dùng	actor	BN (Bệnh nhân), BS (Bác sĩ), Admin
API Gateway	boundary	API_GW (Kong/Nginx)
Microservice	control	auth-svc, appt-svc, notif-svc
Database	database	auth_db, appt_db, med_db
Message Queue	queue	RabbitMQ, Kafka Topic
External System	boundary	VNPay, Twilio, Firebase, GPT-4
Cache	database	Redis Cache
 
2. SD-00 — System Overview Sequence Diagram
Biểu đồ tổng quát mô tả luồng đầy đủ của một bệnh nhân từ khi đăng nhập, đặt lịch khám, khám bệnh, nhận đơn thuốc, đến thanh toán và đánh giá bác sĩ. Đây là "happy path" xuyên suốt toàn bộ 12 microservices.

2.1 Participants & Scope
Participant ID	Loại	Service / Actor
BN	actor	Bệnh nhân (Patient)
BS	actor	Bác sĩ (Doctor)
API_GW	boundary	API Gateway (Kong)
auth-svc	control	S01 auth-service
user-svc	control	S02 user-service
doctor-svc	control	S03 doctor-service
appt-svc	control	S04 appointment-service
notif-svc	control	S05 notification-service
med-rec-svc	control	S06 medical-record-service
ai-svc	control	S07 ai-service
pay-svc	control	S08 payment-service
rx-svc	control	S09 prescription-service
review-svc	control	S10 review-service
MQ	queue	RabbitMQ / Kafka
Redis	database	Redis Cache Cluster

2.2 System Overview — Full Patient Journey
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-00-System-Overview
  title SD-00: Healthcare Platform — Full Patient Journey (Happy Path)
  autonumber
  
  actor       "Bệnh nhân (BN)"  as BN
  actor       "Bác sĩ (BS)"     as BS
  boundary    API_GW            as API_GW   #LightBlue
  control     auth_svc          as AUTH     #SkyBlue
  control     user_svc          as USER     #SkyBlue
  control     doctor_svc        as DOC      #SkyBlue
  control     appt_svc          as APPT     #SkyBlue
  control     notif_svc         as NOTIF    #Khaki
  control     med_rec_svc       as MED      #SkyBlue
  control     ai_svc            as AI       #Plum
  control     pay_svc           as PAY      #LightGreen
  control     rx_svc            as RX       #SkyBlue
  control     review_svc        as REVIEW   #SkyBlue
  database    Redis             as CACHE    #Gold
  queue       RabbitMQ          as MQ       #Orange
  
  == PHASE 1: Authentication ==
  
  BN -> API_GW       : POST /auth/login {email, password}
  API_GW -> AUTH     : forward request
  AUTH -> AUTH       : validate credentials
  AUTH -> CACHE      : SET jwt_blacklist check
  AUTH --> API_GW    : {access_token, refresh_token}
  API_GW --> BN      : 200 OK {tokens}
  
  == PHASE 2: Browse & AI-assisted Doctor Search ==
  
  BN -> API_GW       : GET /doctors?specialty=tim_mach
  API_GW -> USER     : [Bearer token] get doctor list
  USER -> CACHE      : GET doctor_list:tim_mach
  alt Cache HIT
    CACHE --> USER   : cached doctor list
  else Cache MISS
    USER -> USER     : query doctor DB
    USER -> CACHE    : SET doctor_list:tim_mach (TTL 5min)
  end
  USER --> BN        : doctor list with ratings
  
  BN -> API_GW       : POST /ai/recommend-doctor {symptoms}
  API_GW -> AI       : analyze symptoms
  AI -> AI           : NLP symptom analysis (GPT-4)
  AI --> BN          : recommended doctors + specialty
  
  == PHASE 3: Appointment Booking ==
  
  BN -> API_GW       : GET /doctors/{id}/timeslots?date=2026-04-10
  API_GW -> DOC      : get available timeslots
  DOC -> CACHE       : GET slots:{doctorId}:{date}
  DOC --> BN         : available time slots
  
  BN -> API_GW       : POST /appointments {doctorId, slotId, reason}
  API_GW -> APPT     : create appointment
  APPT -> APPT       : optimistic lock slot
  APPT -> DOC        : PATCH /slots/{slotId} status=BOOKED
  DOC -> CACHE       : INVALIDATE slots cache
  APPT -> MQ         : publish appointment.created event
  APPT --> BN        : 201 Created {appointmentId, status: PENDING}
  
  MQ ->> NOTIF       : consume appointment.created
  NOTIF -> NOTIF     : render email/SMS template
  NOTIF ->> BS       : notify new appointment (email + push)
  NOTIF ->> BN       : confirmation notification (email + SMS)
  
  == PHASE 4: Doctor Confirms & Consultation ==
  
  BS -> API_GW       : PATCH /appointments/{id}/confirm
  API_GW -> APPT     : update status PENDING -> CONFIRMED
  APPT -> MQ         : publish appointment.confirmed
  MQ ->> NOTIF       : consume appointment.confirmed
  NOTIF ->> BN       : appointment confirmed notification
  
  note over MQ, NOTIF : 24h & 1h before: scheduler triggers reminder events
  
  BS -> API_GW       : POST /medical-records {appointmentId, soap_note}
  API_GW -> MED      : create medical record
  MED -> MED         : AES-256 encrypt sensitive fields
  MED -> MED         : write immutable audit log
  MED --> BS         : 201 Created {recordId}
  
  APPT -> MQ         : publish appointment.completed
  
  == PHASE 5: Prescription ==
  
  BS -> API_GW       : POST /prescriptions {medications[], appointmentId}
  API_GW -> RX       : create prescription
  RX -> AI           : check drug-drug interactions
  AI --> RX          : interaction report (safe/warning/contraindicated)
  RX -> RX           : apply digital signature (PKI)
  RX -> MQ           : publish prescription.issued
  RX --> BS          : 201 Created {prescriptionId, qr_code}
  MQ ->> NOTIF       : consume prescription.issued
  NOTIF ->> BN       : prescription ready notification
  
  == PHASE 6: Payment ==
  
  BN -> API_GW       : POST /payments/initiate {appointmentId, method: VNPay}
  API_GW -> PAY      : initiate payment
  PAY -> PAY         : generate idempotency key
  PAY -> PAY         : call VNPay gateway
  PAY --> BN         : 200 OK {payment_url}
  BN -> BN           : redirect to VNPay, complete payment
  PAY -> PAY         : receive webhook callback from VNPay
  PAY -> PAY         : validate signature, update transaction COMPLETED
  PAY -> MQ          : publish payment.completed
  MQ ->> NOTIF       : consume payment.completed
  NOTIF ->> BN       : payment receipt + e-invoice notification
  
  == PHASE 7: Review ==
  
  BN -> API_GW       : POST /reviews {doctorId, rating, comment}
  API_GW -> REVIEW   : submit review
  REVIEW -> APPT     : verify appointment COMPLETED (authorization check)
  REVIEW -> REVIEW   : calculate updated doctor rating
  REVIEW -> USER     : PATCH doctor.averageRating
  REVIEW -> MQ       : publish review.submitted
  REVIEW --> BN      : 201 Created {reviewId}
  
  @enduml

📋 Ghi chú kiến trúc SD-00
• Tất cả request từ client phải đi qua API Gateway — không có direct service call từ client.
• API Gateway chịu trách nhiệm: Rate limiting, JWT validation (forward decoded claims), Load balancing.
• Event-driven communication (MQ) được dùng cho cross-service notification để tránh coupling.
• Redis Cache được dùng cho các data read-heavy: doctor list, timeslots, user profile.
• Mỗi write operation quan trọng đều kèm audit log (medical records, prescriptions, payments).

 
3. SD-01 — auth-service: Xác thực & Phân quyền
Service: S01 auth-service   |   Actors: Bệnh nhân, Bác sĩ, Admin, Hệ thống Email/SMS

3.1 SD-01A — Đăng ký & Xác thực Email OTP
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-01A-Register-Login
  title SD-01A: auth-service — Đăng ký tài khoản & Đăng nhập JWT
  autonumber
  
  actor    BN        as BN
  boundary API_GW    as GW
  control  auth_svc  as AUTH
  database auth_db   as DB      #LightYellow
  database Redis     as CACHE   #Gold
  boundary Email_SVC as EMAIL   #LightGray
  
  == UC-01-01: Đăng ký tài khoản ==
  
  BN -> GW         : POST /auth/register {name, email, phone, password, role: PATIENT}
  GW -> AUTH       : forward
  AUTH -> AUTH     : validate input (email format, password strength)
  AUTH -> DB       : SELECT * FROM users WHERE email = ?
  alt Email đã tồn tại
    DB --> AUTH    : user record found
    AUTH --> BN    : 409 Conflict {error: EMAIL_EXISTS}
  else Email chưa tồn tại
    DB --> AUTH    : null
    AUTH -> AUTH   : bcrypt.hash(password, 12)
    AUTH -> DB     : INSERT INTO users (email, password_hash, role=PATIENT, status=PENDING)
    AUTH -> AUTH   : generate 6-digit OTP, expiry = now + 10min
    AUTH -> CACHE  : SET otp:{email} = {otp, expiry} (TTL 10min)
    AUTH -> EMAIL  : sendVerificationEmail(email, otp)
    AUTH --> BN    : 201 Created {message: 'Check your email for OTP'}
  end
  
  == UC-01-02: Xác thực OTP & Kích hoạt tài khoản ==
  
  BN -> GW         : POST /auth/verify-email {email, otp}
  GW -> AUTH       : forward
  AUTH -> CACHE    : GET otp:{email}
  alt OTP hợp lệ & chưa hết hạn
    AUTH -> DB     : UPDATE users SET status=ACTIVE, email_verified=true
    AUTH -> CACHE  : DEL otp:{email}
    AUTH --> BN    : 200 OK {message: 'Account activated'}
  else OTP sai hoặc hết hạn
    AUTH --> BN    : 400 Bad Request {error: INVALID_OTP}
  end
  
  == UC-01-02: Đăng nhập Email + Password ==
  
  BN -> GW         : POST /auth/login {email, password}
  GW -> AUTH       : forward
  AUTH -> DB       : SELECT * FROM users WHERE email = ? AND status = ACTIVE
  alt User không tồn tại hoặc INACTIVE
    AUTH --> BN    : 401 Unauthorized {error: INVALID_CREDENTIALS}
  else User tồn tại
    AUTH -> AUTH   : bcrypt.compare(password, password_hash)
    alt Password không khớp
      AUTH -> DB   : UPDATE failed_login_attempts++
      AUTH --> BN  : 401 Unauthorized
    else Password khớp
      AUTH -> AUTH : JWT sign {userId, role, permissions} exp=15min
      AUTH -> AUTH : generate refresh_token (UUID), exp=7days
      AUTH -> DB   : INSERT INTO refresh_tokens (token, userId, exp, device_info)
      AUTH -> DB   : UPDATE last_login = NOW()
      AUTH --> BN  : 200 OK {access_token, refresh_token, expires_in: 900}
    end
  end
  
  == UC-01-10: Refresh Token ==
  
  BN -> GW         : POST /auth/refresh {refresh_token}
  GW -> AUTH       : forward
  AUTH -> DB       : SELECT * FROM refresh_tokens WHERE token = ? AND exp > NOW()
  alt Token hợp lệ
    AUTH -> CACHE  : GET jwt_blacklist:{userId} (check if forced logout)
    AUTH -> AUTH   : issue new access_token (JWT, exp=15min)
    AUTH -> DB     : UPDATE refresh_token.last_used = NOW()
    AUTH --> BN    : 200 OK {access_token}
  else Token không hợp lệ / hết hạn
    AUTH --> BN    : 401 Unauthorized {error: REFRESH_TOKEN_EXPIRED}
  end
  
  @enduml

3.2 SD-01B — 2FA & RBAC Authorization
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-01B-2FA-RBAC
  title SD-01B: auth-service — 2FA (Doctor) & RBAC Permission Check
  autonumber
  
  actor    BS       as BS
  boundary API_GW   as GW
  control  auth_svc as AUTH
  database auth_db  as DB
  database Redis    as CACHE
  boundary SMS_SVC  as SMS
  
  == UC-01-05: Đăng nhập Bác sĩ với 2FA ==
  
  BS -> GW         : POST /auth/login {doctor_code, password}
  GW -> AUTH       : forward
  AUTH -> DB       : verify credentials, role = DOCTOR
  AUTH -> AUTH     : credentials valid — 2FA required for DOCTOR role
  AUTH -> AUTH     : generate TOTP (time-based 6-digit)
  AUTH -> CACHE    : SET 2fa:{userId}:{session_token} (TTL 5min)
  AUTH -> SMS      : send OTP to doctor's phone
  AUTH --> BS      : 200 OK {requires_2fa: true, session_token}
  
  BS -> GW         : POST /auth/2fa/verify {session_token, otp}
  GW -> AUTH       : forward
  AUTH -> CACHE    : GET 2fa:{userId}:{session_token}
  alt OTP khớp & chưa hết hạn
    AUTH -> CACHE  : DEL 2fa session
    AUTH -> AUTH   : issue JWT {userId, role: DOCTOR, permissions: [...]}
    AUTH --> BS    : 200 OK {access_token, refresh_token}
  else OTP sai (max 3 attempts)
    AUTH -> DB     : log failed 2fa attempt
    AUTH --> BS    : 403 Forbidden {error: INVALID_2FA}
  end
  
  == UC-01-11: RBAC — Permission Gate at API Gateway ==
  
  BS -> GW         : POST /prescriptions [Bearer: doctor_jwt]
  GW -> GW         : decode JWT, extract {role: DOCTOR, permissions}
  GW -> GW         : check route permission: PRESCRIPTION_WRITE ∈ DOCTOR.permissions
  alt Permission granted
    GW -> GW       : forward request with X-User-ID, X-User-Role headers
    note right GW  : Downstream services trust decoded headers\n(no re-validation of JWT)
  else Permission denied
    GW --> BS      : 403 Forbidden {error: INSUFFICIENT_PERMISSIONS}
  end
  
  @enduml

🔒 Ghi chú bảo mật auth-service
• JWT Access Token: 15 phút — ngắn để giảm risk nếu bị lộ. Refresh Token: 7 ngày, stored httpOnly cookie.
• bcrypt cost factor = 12 (balance giữa security và performance, ~250ms/hash).
• RBAC: 4 role chính (PATIENT, DOCTOR, ADMIN, SUPER_ADMIN) + granular permissions per endpoint.
• Rate limiting tại API Gateway: /auth/login max 5 req/min per IP để chống brute force.
• 2FA bắt buộc cho DOCTOR role — bảo vệ quyền truy cập bệnh án và kê đơn thuốc.

 
4. SD-02 — user-service: Quản lý Hồ sơ Người dùng
Service quản lý profile của bệnh nhân và bác sĩ. Là data source chính cho thông tin cá nhân, chuyên môn, và điểm đánh giá. Tích hợp với CDN để lưu ảnh.

4.1 SD-02A — Xem & Cập nhật Hồ sơ + Upload Ảnh
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-02-Profile-Management
  title SD-02: user-service — Quản lý Hồ sơ & Upload Ảnh
  autonumber
  
  actor    BN       as BN
  actor    BS       as BS
  boundary API_GW   as GW
  control  user_svc as USER
  database user_db  as DB
  database Redis    as CACHE
  boundary S3_CDN   as CDN    #LightGray
  
  == UC-02-01: Xem hồ sơ cá nhân (Cached) ==
  
  BN -> GW         : GET /users/me [Bearer token]
  GW -> USER       : X-User-ID: {userId}
  USER -> CACHE    : GET profile:{userId}
  alt Cache HIT
    CACHE --> USER : cached profile data
    USER --> BN    : 200 OK {profile} [X-Cache: HIT]
  else Cache MISS
    USER -> DB     : SELECT * FROM users WHERE id = ?
    USER -> CACHE  : SET profile:{userId} (TTL 5min)
    USER --> BN    : 200 OK {profile} [X-Cache: MISS]
  end
  
  == UC-02-03: Upload ảnh đại diện ==
  
  BN -> GW         : POST /users/avatar (multipart/form-data, file <= 5MB)
  GW -> USER       : forward with X-User-ID
  USER -> USER     : validate: MIME type ∈ {jpg, png, webp}, size <= 5MB
  alt File không hợp lệ
    USER --> BN    : 400 Bad Request {error: INVALID_FILE}
  else File hợp lệ
    USER -> USER   : resize to 256x256, compress (Sharp lib)
    USER -> CDN    : PUT /avatars/{userId}.webp
    CDN --> USER   : {cdn_url}
    USER -> DB     : UPDATE users SET avatar_url = {cdn_url}
    USER -> CACHE  : DEL profile:{userId}
    USER --> BN    : 200 OK {avatar_url}
  end
  
  == UC-02-06/07: Bác sĩ cập nhật hồ sơ chuyên môn ==
  
  BS -> GW         : PATCH /doctors/profile {specialty, bio, experience_years}
  GW -> USER       : X-User-Role: DOCTOR
  USER -> USER     : validate doctor-specific fields
  USER -> DB       : UPDATE doctor_profiles SET ...
  USER -> DB       : INSERT INTO profile_audit_log (userId, changes, timestamp)
  USER -> CACHE    : DEL doctor_list:* (pattern invalidation)
  USER -> CACHE    : DEL profile:{userId}
  USER --> BS      : 200 OK {updated profile}
  
  == UC-02-04/05: Bệnh nhân tìm kiếm bác sĩ ==
  
  BN -> GW         : GET /doctors?specialty=noi-khoa&rating=4&page=1&size=10
  GW -> USER       : forward query params
  USER -> CACHE    : GET doctor_search:{specialty}:{rating}:{page}
  alt Cache MISS
    USER -> DB     : SELECT d.* FROM doctors d\n             JOIN specialties s ON d.specialty_id = s.id\n             WHERE s.slug = ? AND d.avg_rating >= ?\n             ORDER BY d.avg_rating DESC LIMIT 10 OFFSET 0
    USER -> CACHE  : SET doctor_search key (TTL 2min)
  end
  USER --> BN      : 200 OK {doctors: [...], total, pagination}
  
  @enduml

📋 Ghi chú user-service
• Cache invalidation dùng pattern-based DEL (Redis SCAN + DEL) khi doctor profile thay đổi.
• Avatar lưu trên S3-compatible CDN (AWS S3 / MinIO), không lưu trực tiếp trong DB.
• Doctor profile audit log: ghi nhận mọi thay đổi chuyên môn để đảm bảo tính trung thực.

 
5. SD-03 — doctor-service: Lịch làm việc & Quản lý Khám
Service quản lý lịch làm việc của bác sĩ, các time slot khám, ghi chú lâm sàng SOAP. Là dependency quan trọng của appointment-service.

5.1 SD-03 — Quản lý TimeSlot & SOAP Note
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-03-Doctor-Schedule
  title SD-03: doctor-service — Quản lý Lịch & Ghi chú Lâm sàng
  autonumber
  
  actor    BS        as BS
  actor    BN        as BN
  boundary API_GW    as GW
  control  doc_svc   as DOC
  database doctor_db as DB
  database Redis     as CACHE
  queue    RabbitMQ  as MQ
  
  == UC-03-02: Bác sĩ tạo khung giờ khám ==
  
  BS -> GW         : POST /doctors/timeslots
  note right BS    : {date: '2026-04-10',\n slots: [{start:'08:00', end:'08:30'},\n         {start:'08:30', end:'09:00'}, ...]}
  GW -> DOC        : X-User-ID: {doctorId}
  DOC -> DOC       : validate: no overlap with existing slots
  DOC -> DB        : SELECT * FROM timeslots WHERE doctor_id=? AND date=?
  alt Overlap detected
    DOC --> BS     : 409 Conflict {error: SLOT_OVERLAP}
  else No overlap
    DOC -> DB      : BULK INSERT INTO timeslots (doctorId, start, end, status=AVAILABLE)
    DOC -> CACHE   : DEL slots:{doctorId}:{date}
    DOC --> BS     : 201 Created {slots: [...]}
  end
  
  == UC-03-11: Bệnh nhân xem khung giờ trống ==
  
  BN -> GW         : GET /doctors/{id}/timeslots?date=2026-04-10
  GW -> DOC        : forward
  DOC -> CACHE     : GET slots:{doctorId}:2026-04-10
  alt Cache HIT
    CACHE --> DOC  : slot list
  else Cache MISS
    DOC -> DB      : SELECT * FROM timeslots WHERE doctor_id=? AND date=? AND status=AVAILABLE
    DOC -> CACHE   : SET slots:{doctorId}:{date} (TTL 1min — short TTL do slot hay thay đổi)
  end
  DOC --> BN       : 200 OK {available_slots}
  
  == UC-03-08: Ghi chú sau khám (SOAP Note) ==
  
  BS -> GW         : POST /appointments/{apptId}/soap-note
  note right BS    : {S: 'Patient reports...', O: 'BP 120/80...',\n  A: 'Hypertension stage 1', P: 'Amlodipine 5mg OD x 30 days'}
  GW -> DOC        : forward with X-User-ID
  DOC -> DB        : SELECT * FROM appointments WHERE id=? AND doctor_id=? AND status=CONFIRMED
  alt Appointment không hợp lệ
    DOC --> BS     : 403 Forbidden / 404 Not Found
  else Hợp lệ
    DOC -> DB      : INSERT INTO soap_notes (apptId, subjective, objective, assessment, plan)
    DOC -> MQ      : publish soap_note.created {apptId, patientId, note_id}
    DOC -> DB      : UPDATE appointments SET status=COMPLETED
    DOC --> BS     : 201 Created {soap_note_id}
  end
  
  note over MQ : medical-record-service consumes\nsoap_note.created to update patient record
  
  == UC-03-10: Admin duyệt đơn xin nghỉ bác sĩ ==
  
  BS -> GW         : POST /doctors/leave-request {date_from, date_to, reason}
  GW -> DOC        : forward
  DOC -> DB        : INSERT INTO leave_requests
  DOC -> MQ        : publish leave_request.pending
  DOC --> BS       : 202 Accepted {request_id}
  
  @enduml

 
6. SD-04 — appointment-service: Vòng đời Lịch hẹn
Core service xử lý toàn bộ lifecycle của một lịch hẹn: PENDING → CONFIRMED → COMPLETED | CANCELLED | RESCHEDULED. Dùng optimistic locking để chống race condition khi nhiều bệnh nhân đặt cùng slot.

6.1 SD-04 — Đặt lịch, Xác nhận & Hủy lịch
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-04-Appointment-Lifecycle
  title SD-04: appointment-service — Vòng đời Lịch hẹn (Optimistic Locking)
  autonumber
  
  actor    BN        as BN
  actor    BS        as BS
  boundary API_GW    as GW
  control  appt_svc  as APPT
  control  doc_svc   as DOC
  database appt_db   as DB
  database Redis     as CACHE
  queue    RabbitMQ  as MQ
  
  == UC-04-02: Đặt lịch hẹn (Optimistic Locking) ==
  
  BN -> GW         : POST /appointments {doctorId, slotId, reason, type: ONLINE}
  GW -> APPT       : X-User-ID: {patientId}
  activate APPT
  
  APPT -> DOC      : GET /timeslots/{slotId}/lock (request slot lock)
  DOC -> DB        : SELECT status, version FROM timeslots WHERE id=? FOR UPDATE
  alt Slot đã bị đặt (BOOKED)
    DOC --> APPT   : 409 Conflict {error: SLOT_UNAVAILABLE}
    APPT --> BN    : 409 Conflict {error: 'Slot đã được đặt, vui lòng chọn giờ khác'}
  else Slot còn trống (AVAILABLE)
    DOC -> DB      : UPDATE timeslots SET status=LOCKED, version=version+1 WHERE id=? AND version=?
    DOC --> APPT   : 200 OK {lock_token, version}
    APPT -> DB     : INSERT INTO appointments {patientId, doctorId, slotId, status=PENDING, reason}
    APPT -> DOC    : PATCH /timeslots/{slotId} {status: BOOKED, lock_token}
    APPT -> CACHE  : DEL slots:{doctorId}:* (invalidate available slots cache)
    APPT -> MQ     : publish appointment.created {appointmentId, patientId, doctorId, slotTime}
    APPT --> BN    : 201 Created {appointmentId, status: PENDING, slotTime}
  end
  deactivate APPT
  
  == UC-04-05: Đổi lịch hẹn ==
  
  BN -> GW         : PUT /appointments/{id}/reschedule {new_slotId}
  GW -> APPT       : forward
  APPT -> DB       : SELECT * FROM appointments WHERE id=? AND patient_id=? AND status IN (PENDING, CONFIRMED)
  alt Appointment không hợp lệ
    APPT --> BN    : 404 / 400 Bad Request
  else Hợp lệ
    APPT -> DOC    : release old slot: PATCH /timeslots/{old_slotId} {status: AVAILABLE}
    APPT -> DOC    : lock new slot (same optimistic lock flow as above)
    APPT -> DB     : UPDATE appointments SET slot_id=?, status=RESCHEDULED, updated_at=NOW()
    APPT -> MQ     : publish appointment.rescheduled
    APPT --> BN    : 200 OK {appointment}
  end
  
  == UC-04-04: Hủy lịch hẹn (BN) — Refund trigger ==
  
  BN -> GW         : DELETE /appointments/{id} {reason}
  GW -> APPT       : forward
  APPT -> DB       : SELECT * FROM appointments JOIN payments WHERE appt.id=?
  APPT -> APPT     : check cancellation policy (< 24h → partial refund, > 24h → full refund)
  APPT -> DB       : UPDATE appointments SET status=CANCELLED
  APPT -> DOC      : PATCH /timeslots/{slotId} {status: AVAILABLE}
  APPT -> MQ       : publish appointment.cancelled {appointmentId, refund_type, patientId}
  APPT --> BN      : 200 OK {cancellation_confirmed, refund_info}
  
  note over MQ      : payment-service consumes appointment.cancelled\nto initiate refund process
  note over MQ      : notification-service consumes to notify BS & BN
  
  @enduml

⚙️ Ghi chú kỹ thuật appointment-service
• Optimistic locking dùng version field — tránh pessimistic lock làm giảm throughput.
• Slot bị LOCKED tạm thời trong quá trình booking (max 30s) — background job tự release nếu timeout.
• Trạng thái vòng đời: PENDING → CONFIRMED → COMPLETED | CANCELLED | RESCHEDULED.
• Event appointment.created được consumer bởi: notification-svc (gửi xác nhận) và payment-svc (hold fee).

 
7. SD-05 — notification-service: Đa kênh Thông báo
Service xử lý tất cả thông báo hệ thống qua 3 kênh: Email (SMTP/SendGrid), SMS (Twilio), Push Notification (Firebase FCM). Queue-based với retry policy 3 lần, exponential backoff.

7.1 SD-05 — Queue Consumer & Multi-channel Dispatch
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-05-Notification
  title SD-05: notification-service — Queue-based Multi-channel Notification
  autonumber
  
  queue    RabbitMQ    as MQ
  control  notif_svc   as NOTIF
  database notif_db    as DB
  database Redis       as CACHE
  boundary Email_SVC   as EMAIL   #LightGray
  boundary Twilio_SMS  as SMS     #LightGray
  boundary Firebase    as FCM     #LightGray
  actor    BN          as BN
  actor    BS          as BS
  
  == UC-05-01: Gửi thông báo xác nhận lịch hẹn (Event-driven) ==
  
  MQ ->> NOTIF         : consume appointment.confirmed {appointmentId, patientId, doctorId, time}
  activate NOTIF
  NOTIF -> DB          : SELECT notification_prefs WHERE userId IN (patientId, doctorId)
  NOTIF -> NOTIF       : resolve template: 'appointment_confirmed'
  NOTIF -> NOTIF       : render template with appointment data (Handlebars)
  
  group Gửi cho Bệnh nhân
    opt Email enabled
      NOTIF -> EMAIL   : sendEmail({to, subject, html_body})
      alt Email thành công
        EMAIL --> NOTIF: 202 Accepted
        NOTIF -> DB    : INSERT notification_log {userId, channel: EMAIL, status: SENT}
      else Email thất bại
        NOTIF -> NOTIF : schedule retry (30s, 2min, 10min — exponential backoff)
        NOTIF -> DB    : INSERT notification_log {status: FAILED, retry_count++}
      end
    end
    opt SMS enabled
      NOTIF -> SMS     : sendSMS({to: phone, body})
      SMS --> NOTIF    : {messageId, status}
      NOTIF -> DB      : INSERT notification_log {channel: SMS, status}
    end
    opt Push enabled
      NOTIF -> CACHE   : GET fcm_token:{patientId}
      NOTIF -> FCM     : sendPush({token, title, body, data})
      FCM --> NOTIF    : {success: true}
      NOTIF -> DB      : INSERT notification_log {channel: PUSH}
    end
  end
  deactivate NOTIF
  
  == UC-05-02/03: Nhắc lịch tự động (Scheduler) ==
  
  note over NOTIF      : Cron Job chạy mỗi 1 giờ
  NOTIF -> DB          : SELECT appointments WHERE start_time BETWEEN NOW()+23h AND NOW()+25h\n                          AND reminder_24h_sent = false
  loop For each upcoming appointment
    NOTIF -> MQ        : publish appointment.reminder_24h {appointmentId}
  end
  MQ ->> NOTIF         : consume appointment.reminder_24h
  NOTIF -> NOTIF       : render reminder template
  NOTIF ->> BN         : send reminder via preferred channels
  NOTIF -> DB          : UPDATE appointments SET reminder_24h_sent = true
  
  == UC-05-07/08: User xem & đánh dấu đã đọc ==
  
  BN -> NOTIF          : GET /notifications?page=1&size=20 [Bearer]
  NOTIF -> DB          : SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC
  NOTIF --> BN         : {notifications, unread_count}
  
  BN -> NOTIF          : PATCH /notifications/mark-read {ids: [1,2,3]}
  NOTIF -> DB          : UPDATE notifications SET is_read=true WHERE id IN (?) AND user_id=?
  NOTIF -> CACHE       : DEL unread_count:{userId}
  NOTIF --> BN         : 200 OK
  
  @enduml

 
8. SD-06 — medical-record-service: Hồ sơ Bệnh án
Service lưu trữ và quản lý hồ sơ bệnh án điện tử (EHR) tuân thủ HIPAA. Mã hóa AES-256 at-rest, TLS 1.3 in-transit. Mọi truy cập đều ghi immutable audit log.

8.1 SD-06 — Tạo & Chia sẻ Hồ sơ Bệnh án
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-06-Medical-Record
  title SD-06: medical-record-service — Quản lý Hồ sơ Bệnh án (HIPAA Compliant)
  autonumber
  
  actor    BS       as BS
  actor    BN       as BN
  actor    Admin    as ADMIN
  boundary API_GW   as GW
  control  med_svc  as MED
  database med_db   as DB      #LightYellow
  database audit_db as AUDIT   #MistyRose
  boundary S3       as S3      #LightGray
  
  == UC-06-01/02: Tạo & ghi chẩn đoán hồ sơ bệnh án ==
  
  BS -> GW         : POST /medical-records
  note right BS    : {patientId, appointmentId,\n chief_complaint, diagnosis,\n icd10_code, treatment_plan}
  GW -> MED        : X-User-ID: {doctorId}, X-Role: DOCTOR
  activate MED
  MED -> MED       : authorize: doctor must own appointment
  MED -> DB        : SELECT * FROM appointments WHERE id=? AND doctor_id=?
  alt Không có quyền
    MED --> BS     : 403 Forbidden
  else Có quyền
    MED -> MED     : AES-256 encrypt {chief_complaint, diagnosis, treatment_plan}
    MED -> DB      : INSERT INTO medical_records (patientId, doctorId, encrypted_data, icd10_code)
    MED -> AUDIT   : INSERT INTO audit_log {action: CREATE, recordId, userId: doctorId, timestamp, ip}
    MED --> BS     : 201 Created {recordId}
  end
  deactivate MED
  
  == UC-06-04: Xem lịch sử khám bệnh ==
  
  BN -> GW         : GET /medical-records/me?page=1 [Bearer]
  GW -> MED        : X-User-ID: {patientId}
  MED -> DB        : SELECT * FROM medical_records WHERE patient_id=? ORDER BY created_at DESC
  MED -> MED       : AES-256 decrypt each record
  MED -> AUDIT     : INSERT INTO audit_log {action: VIEW, recordId, userId: patientId}
  MED --> BN       : 200 OK {records: [...]}
  
  == UC-06-05: Upload tài liệu y tế (X-quang, Lab results) ==
  
  BS -> GW         : POST /medical-records/{id}/attachments (multipart)
  GW -> MED        : forward file
  MED -> MED       : validate MIME: pdf, jpg, png, dcm (DICOM)
  MED -> S3        : PUT /med-records/{recordId}/{filename} (server-side encryption SSE-S3)
  S3 --> MED       : {s3_key, etag}
  MED -> DB        : INSERT INTO attachments {recordId, s3_key, filename, size, mime_type}
  MED -> AUDIT     : INSERT INTO audit_log {action: UPLOAD_ATTACHMENT}
  MED --> BS       : 201 Created {attachment_id, url}
  
  == UC-06-07/08: Bệnh nhân chia sẻ hồ sơ với bác sĩ khác ==
  
  BN -> GW         : POST /medical-records/{id}/share {target_doctor_id, expires_at}
  GW -> MED        : forward
  MED -> DB        : SELECT * FROM medical_records WHERE id=? AND patient_id=?
  MED -> DB        : INSERT INTO record_shares {recordId, grantedTo: doctorId, grantedBy: patientId, expires_at}
  MED --> BN       : 201 Created {share_token}
  
  note right MED   : Bác sĩ được chia sẻ có quyền VIEW-ONLY\ntrong thời gian expires_at
  
  == UC-06-10: Admin xem audit log ==
  
  ADMIN -> GW      : GET /audit-logs?record_id={id}&date_from=2026-01-01 [ADMIN token]
  GW -> MED        : X-Role: ADMIN
  MED -> AUDIT     : SELECT * FROM audit_log WHERE record_id=? AND created_at >= ?
  MED --> ADMIN    : 200 OK {audit_entries: [{action, userId, timestamp, ip_address}]}
  
  @enduml

🏥 HIPAA Compliance Notes — medical-record-service
• AES-256-GCM encryption for PHI (Protected Health Information) fields at database level.
• Immutable audit log: chỉ INSERT, không UPDATE/DELETE — đảm bảo traceability đầy đủ.
• Record sharing: time-limited, revocable, chỉ VIEW permission (không cho sửa/xóa).
• S3 objects: Server-Side Encryption (SSE-S3), access log enabled, versioning enabled.
• DICOM support (.dcm) cho ảnh y tế chuyên dụng (X-quang, MRI, CT scan).

 
9. SD-07 — ai-service: AI Chatbot, Chẩn đoán & Dự đoán
Service AI tích hợp GPT-4/Gemini cho NLP và Computer Vision cho phân tích ảnh y tế. Bao gồm chatbot tư vấn sức khỏe, gợi ý chẩn đoán cho bác sĩ, và các model dự đoán.

9.1 SD-07A — AI Chatbot & Gợi ý Chuyên khoa
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-07A-AI-Chatbot
  title SD-07A: ai-service — AI Health Chatbot & Symptom Analysis
  autonumber
  
  actor    BN       as BN
  boundary API_GW   as GW
  control  ai_svc   as AI
  database ai_db    as DB      #Plum
  database Redis    as CACHE   #Gold
  boundary GPT4     as LLM     #LightGray
  control  user_svc as USER
  
  == UC-07-01/02/03: AI Chatbot tư vấn sức khỏe ==
  
  BN -> GW         : POST /ai/chat {message, session_id?}
  GW -> AI         : X-User-ID: {patientId}
  activate AI
  AI -> DB         : SELECT conversation_history WHERE session_id=? LIMIT 20
  AI -> AI         : build prompt context (system prompt + history + user message)
  note right AI    : System prompt: 'You are a healthcare assistant.\nDo NOT diagnose. Always recommend consulting a doctor.'
  AI -> LLM        : POST /v1/chat/completions {model: gpt-4, messages: context, max_tokens: 500}
  activate LLM
  LLM --> AI       : {response, tokens_used, finish_reason}
  deactivate LLM
  AI -> AI         : extract entities: {symptoms, body_parts, duration}
  AI -> DB         : INSERT INTO chat_messages {sessionId, role: assistant, content, tokens}
  
  opt Symptoms đủ để gợi ý chuyên khoa
    AI -> AI       : classify symptoms → specialty mapping
    AI -> USER     : GET /doctors?specialty={mapped_specialty}&rating=4
    USER --> AI    : {doctors: [...]}
    AI -> AI       : augment response with doctor recommendations
  end
  
  AI --> BN        : 200 OK {reply, session_id, suggested_specialty?, recommended_doctors?}
  deactivate AI
  
  note over AI, LLM : Disclaimer appended: 'Đây là tư vấn tham khảo.\nKhông thay thế chẩn đoán y khoa.'
  
  == UC-07-04/05: AI hỗ trợ chẩn đoán cho Bác sĩ ==
  
  actor BS as BS
  boundary Vision_API as VISION #LightGray
  
  BS -> GW         : POST /ai/analyze-image {image_base64, image_type: XRAY, patient_id}
  GW -> AI         : X-Role: DOCTOR
  AI -> AI         : validate image: DICOM/JPEG, max 10MB
  AI -> VISION     : POST /v1/images/analyze {image, prompt: 'Analyze this chest X-ray...'}
  VISION --> AI    : {findings, confidence_scores, regions_of_interest}
  AI -> AI         : structure findings into clinical report format
  AI -> DB         : INSERT INTO ai_analysis_logs {doctorId, patientId, image_hash, findings}
  AI --> BS        : 200 OK {analysis_report, confidence, disclaimer}
  
  @enduml

9.2 SD-07B — AI Drug Interaction & Predictive Models
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-07B-AI-Predictions
  title SD-07B: ai-service — Drug Interaction Check & Predictive Analytics
  autonumber
  
  control  rx_svc   as RX
  control  ai_svc   as AI
  database ai_db    as DB
  database drug_db  as DRUG    #LightYellow
  actor    BN       as BN
  control  appt_svc as APPT
  
  == UC-07 (via rx-svc): Kiểm tra tương tác thuốc ==
  
  RX -> AI         : POST /ai/drug-interaction-check
  note right RX    : {medications: [{name, dose, frequency}, ...],\n patient_allergies: [...]}
  activate AI
  AI -> DRUG       : GET /drugs/interactions?drugs={drug_list}
  DRUG --> AI      : {interaction_matrix}
  AI -> AI         : run interaction rules engine
  AI -> AI         : check patient allergies cross-reference
  AI --> RX        : {result: [{pair, severity: NONE|MINOR|MODERATE|SEVERE, description}]}
  deactivate AI
  
  alt Severe interaction detected
    RX -> RX       : block prescription creation
    RX --> BS      : 422 Unprocessable {error: DRUG_INTERACTION_SEVERE, details}
  else Minor/None
    RX -> RX       : continue prescription flow (possibly add warning)
  end
  
  == UC-07-11: Dự đoán thời gian chờ ==
  
  BN -> AI         : GET /ai/predict-wait-time?doctorId={id}&date=2026-04-10
  AI -> DB         : SELECT avg completion_time, current_queue_length for doctor
  AI -> AI         : LSTM model inference: predict wait time
  AI --> BN        : {estimated_wait_minutes: 35, confidence: 0.78}
  
  == UC-07-09: Gợi ý bác sĩ phù hợp (Collaborative Filtering) ==
  
  BN -> AI         : POST /ai/recommend-doctors {symptoms, location, past_appointments}
  AI -> DB         : SELECT patient_history, doctor_vectors FROM embedding_store
  AI -> AI         : cosine similarity: patient_embedding vs doctor_embeddings
  AI -> AI         : filter by availability (call doctor-svc)
  AI --> BN        : {recommended_doctors: [{id, name, score, reason}]}
  
  @enduml

 
10. SD-08 — payment-service: Thanh toán & Hoàn tiền
Service xử lý thanh toán đa cổng (VNPay, MoMo, Stripe), quản lý giao dịch với idempotency key, xử lý webhook callback, và hoàn tiền tuân thủ PCI-DSS.

10.1 SD-08 — Thanh toán VNPay & Hoàn tiền
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-08-Payment
  title SD-08: payment-service — Thanh toán & Hoàn tiền (PCI-DSS)
  autonumber
  
  actor    BN       as BN
  boundary API_GW   as GW
  control  pay_svc  as PAY
  control  appt_svc as APPT
  database pay_db   as DB
  boundary VNPay    as VNPAY   #LightGray
  queue    RabbitMQ as MQ
  
  == UC-08-02/03: Thanh toán phí khám qua VNPay ==
  
  BN -> GW         : POST /payments/initiate {appointmentId, method: VNPAY}
  GW -> PAY        : X-User-ID: {patientId}
  activate PAY
  PAY -> APPT      : GET /appointments/{id}
  APPT --> PAY     : {appointment, fee_amount, doctor_id}
  PAY -> PAY       : generate idempotency_key = hash(patientId + appointmentId + timestamp)
  PAY -> DB        : INSERT INTO transactions {id, patientId, appointmentId, amount, status=PENDING, idempotency_key}
  PAY -> VNPAY     : createPaymentUrl({amount, order_id: transactionId, return_url, ipn_url})
  VNPAY --> PAY    : {payment_url, vnp_TxnRef}
  PAY -> DB        : UPDATE transactions SET vnp_txn_ref = ?
  PAY --> BN       : 200 OK {payment_url, transaction_id}
  deactivate PAY
  
  BN -> VNPAY      : user completes payment on VNPay portal
  VNPAY -> PAY     : POST /payments/vnpay/ipn (webhook callback)
  note right VNPAY : IPN: Instant Payment Notification\n{vnp_ResponseCode, vnp_Amount,\n vnp_SecureHash, vnp_TxnRef}
  activate PAY
  PAY -> PAY       : verify HMAC-SHA512 signature
  alt Signature invalid
    PAY --> VNPAY  : {RspCode: '97', Message: 'Invalid signature'}
  else Signature valid
    PAY -> DB      : SELECT * FROM transactions WHERE vnp_txn_ref=?
    alt Duplicate IPN (already processed)
      PAY --> VNPAY: {RspCode: '02', Message: 'Order already confirmed'}
    else Chưa xử lý
      PAY -> DB    : UPDATE transactions SET status=COMPLETED, paid_at=NOW()
      PAY -> MQ    : publish payment.completed {transactionId, appointmentId, patientId, amount}
      PAY --> VNPAY: {RspCode: '00', Message: 'Confirm Success'}
    end
  end
  deactivate PAY
  
  == UC-08-07/08: Yêu cầu & Xử lý hoàn tiền ==
  
  BN -> GW         : POST /payments/{txnId}/refund {reason}
  GW -> PAY        : forward
  PAY -> DB        : SELECT * FROM transactions WHERE id=? AND patient_id=? AND status=COMPLETED
  PAY -> PAY       : check refund policy (appointment cancelled, < 24h = 70%, > 24h = 100%)
  PAY -> DB        : INSERT INTO refund_requests {txnId, amount, status=PENDING}
  PAY -> MQ        : publish refund.requested
  PAY --> BN       : 202 Accepted {refund_request_id, estimated_days: 3-5}
  
  note over PAY    : Admin approves refund → PAY calls VNPay refund API
  
  @enduml

💳 PCI-DSS Compliance Notes — payment-service
• Không lưu raw card data — chỉ lưu masked number và token từ payment gateway.
• Idempotency key ngăn duplicate payment khi user bấm F5 hoặc retry.
• IPN webhook: verify HMAC-SHA512 signature trước khi xử lý bất kỳ action nào.
• Refund flow: async qua queue, Admin approval trước khi gọi gateway refund API.

 
11. SD-09 — prescription-service: Đơn thuốc Điện tử
Quản lý đơn thuốc điện tử với chữ ký số PKI, tích hợp AI kiểm tra tương tác thuốc, QR code xác minh tại nhà thuốc.
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-09-Prescription
  title SD-09: prescription-service — Đơn thuốc Điện tử & Chữ ký số
  autonumber
  
  actor    BS       as BS
  actor    BN       as BN
  actor    Pharmacy as PHARM
  boundary API_GW   as GW
  control  rx_svc   as RX
  control  ai_svc   as AI
  database rx_db    as DB
  boundary PKI_SVC  as PKI
  queue    RabbitMQ as MQ
  
  == UC-09-01 to 05: Tạo & Ký đơn thuốc ==
  
  BS -> GW         : POST /prescriptions {appointmentId, medications: [{name, dose, frequency, days}]}
  GW -> RX         : X-User-ID: {doctorId}
  RX -> AI         : POST /ai/drug-interaction-check {medications, patient_allergies}
  AI --> RX        : {interactions: [{severity: NONE|MINOR|MODERATE|SEVERE}]}
  alt SEVERE interaction
    RX --> BS      : 422 {error: CONTRAINDICATED, details}
  else MODERATE (warning)
    RX -> DB       : INSERT INTO prescriptions {status=DRAFT, interaction_warning=true}
    RX --> BS      : 201 Created with warning {requires_override: true}
  else NONE/MINOR
    RX -> DB       : INSERT INTO prescriptions {status=DRAFT}
    RX --> BS      : 201 Created {prescriptionId}
  end
  
  BS -> GW         : POST /prescriptions/{id}/sign
  GW -> RX         : forward
  RX -> PKI        : sign({prescriptionData, doctor_certificate_id})
  PKI -> PKI       : create digital signature (RSA-2048 + SHA-256)
  PKI --> RX       : {signature, certificate_chain}
  RX -> RX         : generate QR code (contains: rx_id + signature_hash)
  RX -> DB         : UPDATE prescriptions SET status=SIGNED, signature=?, qr_code=?
  RX -> MQ         : publish prescription.issued {prescriptionId, patientId}
  RX --> BS        : 200 OK {prescription_url, qr_code_image}
  
  == UC-09-06/07: Bệnh nhân xem & tải đơn thuốc ==
  
  BN -> GW         : GET /prescriptions/{id} [Bearer]
  GW -> RX         : X-User-ID: {patientId}
  RX -> DB         : SELECT * FROM prescriptions WHERE id=? AND patient_id=?
  RX -> RX         : verify: patient must be linked to prescription
  RX --> BN        : 200 OK {prescription_data, qr_code, download_url}
  
  == UC-09-08/09: Nhà thuốc xác nhận & cấp phát ==
  
  PHARM -> RX      : POST /prescriptions/verify {qr_code_data}
  RX -> PKI        : verify_signature(qr_code_data)
  PKI --> RX       : {valid: true, doctor_info, signed_at}
  RX -> DB         : SELECT * FROM prescriptions WHERE id=? AND status=SIGNED AND dispensed=false
  alt Đơn đã cấp phát
    RX --> PHARM   : 409 {error: ALREADY_DISPENSED}
  else Chưa cấp phát
    PHARM -> RX    : POST /prescriptions/{id}/dispense {pharmacy_id, pharmacist_id}
    RX -> DB       : UPDATE prescriptions SET dispensed=true, dispensed_at=NOW(), dispensed_by=?
    RX --> PHARM   : 200 OK {confirmation}
  end
  
  @enduml

 
12. SD-10 — review-service: Đánh giá Bác sĩ
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-10-Review
  title SD-10: review-service — Đánh giá Bác sĩ & Kiểm duyệt
  autonumber
  
  actor    BN        as BN
  actor    BS        as BS
  actor    Admin     as ADMIN
  boundary API_GW    as GW
  control  review_svc as REV
  control  appt_svc  as APPT
  control  user_svc  as USER
  database review_db as DB
  
  == UC-10-01/02/03: Bệnh nhân đánh giá bác sĩ ==
  
  BN -> GW         : POST /reviews {doctorId, appointmentId, rating: {expertise:5, attitude:4, punctuality:5, clarity:4}, comment}
  GW -> REV        : X-User-ID: {patientId}
  REV -> APPT      : GET /appointments/{appointmentId}
  APPT --> REV     : {appointment}
  REV -> REV       : validate: patient_id matches, status=COMPLETED, no existing review
  alt Validation fails
    REV --> BN     : 403/409 {error}
  else Hợp lệ
    REV -> DB      : INSERT INTO reviews {patientId, doctorId, apptId, scores, comment, status=PENDING_MODERATION}
    REV -> REV     : calculate weighted_score = avg(expertise, attitude, punctuality, clarity)
    REV --> BN     : 201 Created {reviewId, status: pending_moderation}
  end
  
  == UC-10-08/09: Admin kiểm duyệt ==
  
  ADMIN -> REV     : GET /reviews?status=PENDING_MODERATION
  REV -> DB        : SELECT * FROM reviews WHERE status=PENDING_MODERATION
  REV --> ADMIN    : {reviews: [...]}
  
  ADMIN -> REV     : PATCH /reviews/{id}/moderate {action: APPROVE}
  REV -> DB        : UPDATE reviews SET status=PUBLISHED, published_at=NOW()
  REV -> REV       : recalculate doctor.avg_rating
  REV -> USER      : PATCH /doctors/{id}/rating {new_avg_rating, total_reviews}
  REV --> ADMIN    : 200 OK
  
  == UC-10-05: Bác sĩ phản hồi đánh giá ==
  
  BS -> REV        : POST /reviews/{id}/reply {reply_text}
  REV -> DB        : SELECT * FROM reviews WHERE id=? — verify doctor_id matches
  REV -> DB        : INSERT INTO review_replies {reviewId, doctorId, reply_text}
  REV --> BS       : 201 Created {replyId}
  
  @enduml

 
13. SD-11 — report-service: Báo cáo & Business Intelligence
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-11-Report
  title SD-11: report-service — BI Dashboard & Báo cáo tự động
  autonumber
  
  actor    Admin    as ADMIN
  actor    BS       as BS
  boundary API_GW   as GW
  control  rpt_svc  as RPT
  database dw_db    as DW     #LightBlue
  database rpt_db   as DB
  boundary Email    as EMAIL
  
  == UC-11-01/02: Admin xem báo cáo doanh thu ==
  
  ADMIN -> GW      : GET /reports/revenue?from=2026-01-01&to=2026-03-31&group_by=month
  GW -> RPT        : X-Role: ADMIN
  RPT -> DW        : SELECT SUM(amount), COUNT(*), AVG(amount)\n         FROM fact_payments\n         WHERE paid_at BETWEEN ? AND ?\n         GROUP BY MONTH(paid_at)
  DW --> RPT       : {monthly_revenue: [...]}
  RPT -> RPT       : format response, compute YoY growth
  RPT --> ADMIN    : 200 OK {report_data, summary_stats}
  
  == UC-11-05: Xuất báo cáo Excel/PDF ==
  
  ADMIN -> GW      : POST /reports/export {type: REVENUE, format: EXCEL, period: Q1_2026}
  GW -> RPT        : forward
  RPT -> DW        : fetch full dataset
  RPT -> RPT       : generate Excel (ExcelJS) / PDF (PDFKit)
  RPT -> DB        : INSERT INTO export_jobs {status: COMPLETED, file_key}
  RPT --> ADMIN    : 200 OK {download_url, expires_in: 3600}
  
  == UC-11-09/10: Báo cáo tự động theo lịch ==
  
  note over RPT    : Cron Job: mỗi ngày đầu tuần 08:00
  RPT -> RPT       : trigger weekly_report_generation
  RPT -> DW        : fetch weekly KPIs
  RPT -> RPT       : render HTML email report
  RPT -> EMAIL     : send to all ADMIN emails
  RPT -> DB        : INSERT INTO scheduled_report_log
  
  @enduml

 
14. SD-12 — admin-service: Quản trị Hệ thống
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-12-Admin
  title SD-12: admin-service — Quản trị & Monitor Hệ thống
  autonumber
  
  actor    Admin      as ADMIN
  actor    SuperAdmin as SA
  boundary API_GW     as GW
  control  admin_svc  as ADM
  database admin_db   as DB
  database audit_db   as AUDIT
  boundary Prometheus as PROM  #LightGray
  
  == UC-12-06: Duyệt hồ sơ bác sĩ mới ==
  
  ADMIN -> GW      : GET /admin/doctors/pending
  GW -> ADM        : X-Role: ADMIN
  ADM -> DB        : SELECT * FROM doctor_applications WHERE status=PENDING
  ADM --> ADMIN    : {pending_doctors: [...]}
  
  ADMIN -> GW      : PATCH /admin/doctors/{id}/approve {notes}
  GW -> ADM        : forward
  ADM -> DB        : UPDATE doctor_profiles SET status=APPROVED, approved_by=?
  ADM -> ADM       : call user-svc: activate doctor account
  ADM -> AUDIT     : INSERT audit_log {action: DOCTOR_APPROVED, adminId, doctorId}
  ADM --> ADMIN    : 200 OK
  
  == UC-12-07: Xem audit log hệ thống ==
  
  SA -> GW         : GET /admin/audit-logs?action=PAYMENT_REFUND&date_from=2026-01-01
  GW -> ADM        : X-Role: SUPER_ADMIN
  ADM -> AUDIT     : SELECT * FROM system_audit_log WHERE action=? AND created_at >= ?
  ADM --> SA       : {audit_entries: [{action, actor, target, timestamp, ip, changes}]}
  
  == UC-12-10: Monitor health check services ==
  
  SA -> GW         : GET /admin/system/health
  GW -> ADM        : forward
  ADM -> PROM      : GET /api/v1/query {query: up{job=~'.*-service'}}
  PROM --> ADM     : {service_health: [{name, status: UP/DOWN, latency_p99}]}
  ADM -> ADM       : aggregate: all_healthy / degraded / critical
  ADM --> SA       : 200 OK {system_status, services: [...]}
  
  == UC-12-09: Backup dữ liệu ==
  
  SA -> GW         : POST /admin/backup/trigger {scope: FULL}
  GW -> ADM        : X-Role: SUPER_ADMIN
  ADM -> ADM       : trigger pg_dump for each service DB
  ADM -> ADM       : encrypt backup (AES-256)
  ADM -> ADM       : upload to S3 Glacier
  ADM -> AUDIT     : INSERT audit_log {action: BACKUP_TRIGGERED, superAdminId}
  ADM --> SA       : 202 Accepted {backup_job_id}
  
  @enduml

 
15. Ma Trận Phụ Thuộc Service (Service Dependency Matrix)
Bảng dưới mô tả các phụ thuộc runtime giữa các service. "SYNC" = synchronous REST/gRPC call, "ASYNC" = event-driven qua message queue, "-" = không có phụ thuộc trực tiếp.

Caller \ Callee	auth	user	doctor	appt	notif	med-rec	ai	pay
auth-svc	-	SYNC	-	-	-	-	-	-
user-svc	SYNC	-	-	-	-	-	-	-
doctor-svc	SYNC	SYNC	-	-	ASYNC	-	-	-
appt-svc	-	-	SYNC	-	ASYNC	-	-	ASYNC
notif-svc	-	SYNC	-	-	-	-	-	-
med-rec-svc	SYNC	-	-	SYNC	ASYNC	-	-	-
ai-svc	-	SYNC	SYNC	-	-	SYNC	-	-
pay-svc	-	-	-	SYNC	ASYNC	-	-	-
rx-svc	-	-	-	SYNC	ASYNC	-	SYNC	-

📊 Phân tích phụ thuộc
• SYNC calls: tạo tight coupling — cần circuit breaker (Hystrix/Resilience4j) để tránh cascade failure.
• ASYNC via Queue: loose coupling — consumer có thể scale độc lập, retry tự động khi service down.
• appt-svc là hub trung tâm: phụ thuộc nhiều service → cần ưu tiên SLA và monitoring cao nhất.
• ai-svc không được gọi bởi auth/notif/pay → isolation tốt, có thể scale GPU resources độc lập.

 
16. Error Handling & Non-Happy Path Flows
16.1 SD-ERR01 — Circuit Breaker & Service Timeout
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-ERR01-Circuit-Breaker
  title SD-ERR01: Circuit Breaker Pattern — ai-service Fallback
  autonumber
  
  actor    BN       as BN
  boundary API_GW   as GW
  control  appt_svc as APPT
  control  ai_svc   as AI
  database Redis    as CACHE
  
  BN -> GW         : POST /appointments/book-with-ai-suggest {symptoms}
  GW -> APPT       : forward
  APPT -> AI       : POST /ai/recommend-doctor {symptoms} [timeout: 3s]
  
  alt ai-svc responds within 3s
    AI --> APPT    : {recommended_doctors}
    APPT --> BN    : 200 OK with AI suggestions
  else ai-svc timeout / circuit OPEN
    note over APPT : Circuit Breaker trạng thái OPEN\n(>50% failures in last 10s)
    APPT -> CACHE  : GET fallback_doctor_list:popular
    CACHE --> APPT : {popular_doctors} (pre-cached)
    APPT --> BN    : 200 OK {doctors: fallback_list, ai_unavailable: true}
  end
  
  @enduml

16.2 SD-ERR02 — Distributed Transaction Saga (Booking + Payment)
  ┌─ Sequence Diagram (PlantUML)  
  @startuml SD-ERR02-Saga
  title SD-ERR02: Saga Pattern — Compensating Transaction khi Payment thất bại
  autonumber
  
  actor    BN       as BN
  control  appt_svc as APPT
  control  pay_svc  as PAY
  control  doc_svc  as DOC
  queue    MQ       as MQ
  
  BN -> APPT       : book appointment
  APPT -> DOC      : lock timeslot (BOOKED)
  APPT -> MQ       : publish appointment.created
  APPT --> BN      : 201 Created {appointmentId}
  
  BN -> PAY        : initiate payment
  PAY -> PAY       : call VNPay gateway
  PAY -> PAY       : VNPay returns FAILURE (insufficient funds)
  
  note over PAY    : SAGA Compensation triggered
  
  PAY -> MQ        : publish payment.failed {appointmentId}
  MQ ->> APPT      : consume payment.failed
  APPT -> APPT     : compensate: UPDATE appointment SET status=CANCELLED
  APPT -> DOC      : compensate: PATCH /timeslots/{id} {status: AVAILABLE}
  APPT -> MQ       : publish appointment.cancelled (compensation complete)
  
  BN <-- APPT      : async notification: 'Thanh toán thất bại, lịch đã được huỷ'
  
  @enduml

16.3 Bảng Mã Lỗi Chuẩn Hóa
HTTP Status	Error Code	Service	Mô tả & Xử lý
400	VALIDATION_ERROR	All services	Input không hợp lệ — trả về field errors chi tiết
401	UNAUTHORIZED	auth-svc	Token không hợp lệ/hết hạn — client phải refresh
403	INSUFFICIENT_PERMISSIONS	API Gateway	Role không đủ quyền — không retry
404	RESOURCE_NOT_FOUND	All services	Resource không tồn tại — không retry
409	CONFLICT	appt-svc, review-svc	Race condition / duplicate — retry với new slot
422	DRUG_INTERACTION_SEVERE	rx-svc	Tương tác thuốc nghiêm trọng — yêu cầu override
429	RATE_LIMIT_EXCEEDED	API Gateway	Quá rate limit — Retry-After header chỉ thời gian chờ
500	INTERNAL_SERVER_ERROR	All services	Lỗi nội bộ — log, alert, trả về request_id để trace
503	SERVICE_UNAVAILABLE	API Gateway	Circuit breaker OPEN — trả về fallback data nếu có


