

HEALTHCARE PLATFORM
Tài liệu Use Case Diagram – 12 Microservices


13
Use Case Diagrams	127
Use Cases	12
Microservices

Phiên bản: 1.0     |     Năm: 2026     |     Kiến trúc: Microservices

 
1. Tổng quan 12 Microservices
Kiến trúc hệ thống và danh sách services

Healthcare Platform được xây dựng theo kiến trúc Microservices gồm 12 service độc lập, giao tiếp qua REST API / gRPC / Message Queue. Tổng cộng 13 Use Case Diagram với 127 use case phân theo 5 nhóm actor chính.

ID	Service	UCD	UC#	Actors	Mô tả
S01	auth-service	UCD-01	12	BN, BS, Admin, Email/SMS	Xác thực, phân quyền RBAC, JWT, OTP, 2FA
S02	user-service	UCD-02	10	BN, BS, Admin	Hồ sơ người dùng, tìm kiếm, quản lý tài khoản
S03	doctor-service	UCD-03	11	BS, Admin, BN	Lịch làm việc, khung giờ, ghi chú lâm sàng
S04	appointment-service	UCD-04	10	BN, BS, Notification	Đặt/hủy/đổi lịch hẹn, quản lý vòng đời
S05	notification-service	UCD-05	10	BN, BS, Email, SMS, Firebase	Đa kênh: Email, SMS, Push Notification
S06	medical-record-service	UCD-06	10	BS, BN, Admin	Hồ sơ bệnh án, HIPAA, HL7 FHIR, audit log
S07	ai-service	UCD-07a/07b	14	BN, BS, AI Engine	Chatbot, chẩn đoán AI, gợi ý lịch, phân tích
S08	payment-service	UCD-08	10	BN, Admin, VNPay/Momo/Stripe	Thanh toán đa cổng, hóa đơn VAT, hoàn tiền
S09	prescription-service	UCD-09	10	BS, BN, Nhà thuốc	Đơn thuốc điện tử, chữ ký số, drug interaction
S10	review-service	UCD-10	10	BN, BS, Admin	Đánh giá bác sĩ, kiểm duyệt, xếp hạng
S11	report-service	UCD-11	10	Admin, BS, Hệ thống BI	Báo cáo doanh thu, thống kê, export Excel/PDF
S12	admin-service	UCD-12	10	Admin, Super Admin	Quản trị hệ thống, RBAC, audit log, monitor

Danh sách Actor & Role


Actor	Role	Mô tả quyền hạn
Bệnh nhân (BN)	PATIENT	Đăng ký, đặt lịch, xem hồ sơ, thanh toán, đánh giá bác sĩ, chat AI
Bác sĩ (BS)	DOCTOR	Quản lý lịch làm việc, xác nhận lịch hẹn, ghi chú lâm sàng, kê đơn
Admin	ADMIN	Quản lý người dùng, duyệt bác sĩ, kiểm duyệt nội dung, xem báo cáo
Super Admin	SUPER_ADMIN	Toàn quyền hệ thống, cấu hình, phân quyền, backup, monitor
Nhà thuốc	PHARMACIST	Xem và xác nhận cấp phát đơn thuốc điện tử
Hệ thống (System)	SYSTEM	Actor tự động: Notification Engine, AI Engine, BI, Payment Gateway
 
2. [UCD-01] auth-service
Xác thực & Phân quyền


UCD-01	Actors: Bệnh nhân | Bác sĩ | Admin | Hệ thống Email/SMS

 
Hình 2: Xác thực & Phân quyền — auth-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-01-01	Đăng ký tài khoản	Bệnh nhân
UC-01-02	Đăng nhập bằng Email / Mật khẩu	Bệnh nhân, Bác sĩ, Admin
UC-01-03	Đăng nhập bằng SĐT + OTP	Bệnh nhân, Bác sĩ
UC-01-04	Đăng nhập bằng mã bác sĩ	Bác sĩ
UC-01-05	Xác thực 2 bước (2FA)	Bác sĩ
UC-01-06	Đăng xuất (Logout)	Bệnh nhân, Bác sĩ, Admin
UC-01-07	Quên mật khẩu	Bệnh nhân, Bác sĩ
UC-01-08	Đặt lại mật khẩu	Bệnh nhân, Bác sĩ
UC-01-09	Đổi mật khẩu	Bệnh nhân, Bác sĩ, Admin
UC-01-10	Làm mới Token (Refresh Token)	Hệ thống
UC-01-11	Phân quyền truy cập (RBAC)	Hệ thống
UC-01-12	Vô hiệu hóa tài khoản	Admin

NOTE	RBAC phân quyền 4 role: Patient, Doctor, Admin, System. JWT Access Token (15 phút) + Refresh Token (7 ngày).
 
3. [UCD-02] user-service
Quản lý Hồ sơ Người dùng


UCD-02	Actors: Bệnh nhân | Bác sĩ | Admin

 
Hình 3: Quản lý Hồ sơ Người dùng — user-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-02-01	Xem hồ sơ cá nhân	Bệnh nhân, Bác sĩ, Admin
UC-02-02	Cập nhật thông tin cá nhân	Bệnh nhân, Bác sĩ
UC-02-03	Upload ảnh đại diện	Bệnh nhân, Bác sĩ
UC-02-04	Xem danh sách bác sĩ	Bệnh nhân
UC-02-05	Xem chi tiết hồ sơ bác sĩ	Bệnh nhân
UC-02-06	Quản lý hồ sơ chuyên môn	Bác sĩ
UC-02-07	Cập nhật chứng chỉ hành nghề	Bác sĩ
UC-02-08	Quản lý tài khoản người dùng	Admin
UC-02-09	Tìm kiếm người dùng	Admin
UC-02-10	Khoá / Mở khoá tài khoản	Admin

NOTE	Doctor profile bao gồm chuyên môn, chứng chỉ, kinh nghiệm, ảnh đại diện và điểm đánh giá trung bình.
 
4. [UCD-03] doctor-service
Quản lý Bác sĩ & Lịch làm việc


UCD-03	Actors: Bác sĩ | Admin | Bệnh nhân

 
Hình 4: Quản lý Bác sĩ & Lịch làm việc — doctor-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-03-01	Quản lý lịch làm việc	Bác sĩ
UC-03-02	Thêm khung giờ khám	Bác sĩ
UC-03-03	Sửa khung giờ khám	Bác sĩ
UC-03-04	Xóa khung giờ khám	Bác sĩ
UC-03-05	Xem danh sách lịch hẹn của mình	Bác sĩ
UC-03-06	Xác nhận lịch hẹn bệnh nhân	Bác sĩ
UC-03-07	Hủy lịch hẹn (phía bác sĩ)	Bác sĩ
UC-03-08	Ghi chú sau khám (SOAP Note)	Bác sĩ
UC-03-09	Xem lịch sử khám bệnh nhân	Bác sĩ
UC-03-10	Duyệt đơn xin nghỉ của bác sĩ	Admin
UC-03-11	Xem khung giờ còn trống	Bệnh nhân

NOTE	TimeSlot có trạng thái: AVAILABLE, BOOKED, BLOCKED. SOAP Note: Subjective, Objective, Assessment, Plan.
 
5. [UCD-04] appointment-service
Đặt lịch & Quản lý Lịch hẹn


UCD-04	Actors: Bệnh nhân | Bác sĩ | Hệ thống Notification

 
Hình 5: Đặt lịch & Quản lý Lịch hẹn — appointment-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-04-01	Xem lịch & khung giờ trống	Bệnh nhân
UC-04-02	Đặt lịch hẹn	Bệnh nhân
UC-04-03	Xem chi tiết lịch hẹn	Bệnh nhân, Bác sĩ
UC-04-04	Hủy lịch hẹn (BN)	Bệnh nhân
UC-04-05	Đổi lịch hẹn	Bệnh nhân
UC-04-06	Xác nhận lịch hẹn (BS)	Bác sĩ
UC-04-07	Từ chối lịch hẹn (BS)	Bác sĩ
UC-04-08	Đánh dấu hoàn thành khám	Bác sĩ
UC-04-09	Gửi thông báo xác nhận	Hệ thống
UC-04-10	Gửi nhắc lịch tự động	Hệ thống

NOTE	Vòng đời Appointment: PENDING → CONFIRMED → COMPLETED | CANCELLED | RESCHEDULED. Optimistic locking chống race condition.
 
6. [UCD-05] notification-service
Quản lý Thông báo


UCD-05	Actors: Bệnh nhân | Bác sĩ | Hệ thống Email | SMS | Firebase Push

 
Hình 6: Quản lý Thông báo — notification-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-05-01	Gửi thông báo xác nhận lịch hẹn	Hệ thống
UC-05-02	Gửi nhắc lịch 24h trước	Hệ thống
UC-05-03	Gửi nhắc lịch 1h trước	Hệ thống
UC-05-04	Gửi thông báo hủy lịch	Hệ thống
UC-05-05	Gửi thông báo kết quả xét nghiệm	Hệ thống
UC-05-06	Gửi thông báo đơn thuốc sẵn sàng	Hệ thống
UC-05-07	Xem danh sách thông báo	Bệnh nhân, Bác sĩ
UC-05-08	Đánh dấu đã đọc	Bệnh nhân, Bác sĩ
UC-05-09	Cài đặt tùy chọn thông báo	Bệnh nhân, Bác sĩ
UC-05-10	Gửi thông báo hàng loạt (Broadcast)	Admin

NOTE	Queue-based (RabbitMQ/Kafka). Retry policy: 3 lần, exponential backoff. Đa kênh: SMTP, Twilio SMS, Firebase FCM.
 
7. [UCD-06] medical-record-service
Quản lý Hồ sơ Bệnh án


UCD-06	Actors: Bác sĩ | Bệnh nhân | Admin

 
Hình 7: Quản lý Hồ sơ Bệnh án — medical-record-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-06-01	Tạo hồ sơ bệnh án mới	Bác sĩ
UC-06-02	Ghi chẩn đoán & điều trị	Bác sĩ
UC-06-03	Cập nhật hồ sơ bệnh án	Bác sĩ
UC-06-04	Xem lịch sử khám bệnh	Bác sĩ, Bệnh nhân
UC-06-05	Upload tài liệu y tế	Bác sĩ
UC-06-06	Tải hồ sơ bệnh án (PDF)	Bác sĩ, Bệnh nhân
UC-06-07	Chia sẻ hồ sơ với bác sĩ khác	Bệnh nhân
UC-06-08	Xem hồ sơ được chia sẻ	Bác sĩ
UC-06-09	Quản lý quyền truy cập hồ sơ	Bệnh nhân
UC-06-10	Audit log truy cập hồ sơ	Admin

NOTE	Tuân thủ HIPAA. Mã hóa AES-256 at rest, TLS 1.3 in transit. Mọi truy cập ghi immutable audit log.
 
8. [UCD-07a] ai-service
AI Hỗ trợ Chẩn đoán & Chatbot


UCD-07a	Actors: Bệnh nhân | Bác sĩ | AI Engine

 
Hình 8: AI Hỗ trợ Chẩn đoán & Chatbot — ai-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-07-01	Chat với AI Chatbot sức khỏe	Bệnh nhân
UC-07-02	Mô tả triệu chứng cho AI	Bệnh nhân
UC-07-03	Nhận gợi ý chuyên khoa phù hợp	Bệnh nhân
UC-07-04	AI phân tích hình ảnh y tế	Bác sĩ
UC-07-05	AI gợi ý chẩn đoán sơ bộ	Bác sĩ
UC-07-06	AI gợi ý phác đồ điều trị	Bác sĩ
UC-07-07	Xem lịch sử hội thoại AI	Bệnh nhân
UC-07-08	Đánh giá độ chính xác AI	Bác sĩ

NOTE	Tích hợp GPT-4/Gemini cho NLP. Computer Vision cho X-quang, MRI, CT scan. Disclaimer: không thay thế chẩn đoán y khoa.
 
9. [UCD-07b] ai-service
AI Gợi ý Lịch hẹn & Dự đoán


UCD-07b	Actors: Bệnh nhân | Bác sĩ | AI Engine

 
Hình 9: AI Gợi ý Lịch hẹn & Dự đoán — ai-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-07-09	AI gợi ý bác sĩ phù hợp	Bệnh nhân
UC-07-10	AI gợi ý thời gian tái khám	Bác sĩ
UC-07-11	Dự đoán thời gian chờ khám	Bệnh nhân
UC-07-12	Phân tích xu hướng bệnh (Analytics)	Bác sĩ
UC-07-13	Cảnh báo rủi ro sức khỏe	Bệnh nhân
UC-07-14	Tóm tắt hồ sơ bệnh án bằng AI	Bác sĩ

NOTE	ML Models: Collaborative Filtering (gợi ý bác sĩ), LSTM Time-series (dự đoán thời gian chờ), NLP Summarization (tóm tắt hồ sơ).
 
10. [UCD-08] payment-service
Thanh toán & Hóa đơn


UCD-08	Actors: Bệnh nhân | Admin | Cổng thanh toán (VNPay / Momo / Stripe)

 
Hình 10: Thanh toán & Hóa đơn — payment-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-08-01	Xem chi phí khám bệnh	Bệnh nhân
UC-08-02	Thanh toán phí khám (Online)	Bệnh nhân
UC-08-03	Thanh toán qua VNPay	Bệnh nhân
UC-08-04	Thanh toán qua Momo	Bệnh nhân
UC-08-05	Thanh toán qua thẻ Visa/Mastercard	Bệnh nhân
UC-08-06	Xem lịch sử giao dịch	Bệnh nhân, Admin
UC-08-07	Yêu cầu hoàn tiền	Bệnh nhân
UC-08-08	Xử lý hoàn tiền	Admin
UC-08-09	Xuất hóa đơn điện tử (VAT)	Bệnh nhân, Admin
UC-08-10	Quản lý doanh thu & báo cáo tài chính	Admin

NOTE	Tuân thủ PCI-DSS. Webhook xử lý payment callback. Idempotency key chống duplicate payment.
 
11. [UCD-09] prescription-service
Quản lý Đơn thuốc


UCD-09	Actors: Bác sĩ | Bệnh nhân | Nhà thuốc

 
Hình 11: Quản lý Đơn thuốc — prescription-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-09-01	Tạo đơn thuốc điện tử	Bác sĩ
UC-09-02	Thêm thuốc vào đơn	Bác sĩ
UC-09-03	Kiểm tra tương tác thuốc (AI)	Bác sĩ
UC-09-04	Ký điện tử đơn thuốc	Bác sĩ
UC-09-05	Gửi đơn thuốc cho bệnh nhân	Bác sĩ
UC-09-06	Xem đơn thuốc	Bệnh nhân
UC-09-07	Tải đơn thuốc (PDF)	Bệnh nhân
UC-09-08	Xác nhận cấp phát thuốc	Nhà thuốc
UC-09-09	Kiểm tra đơn thuốc hợp lệ	Nhà thuốc
UC-09-10	Lịch sử đơn thuốc	Bác sĩ, Bệnh nhân

NOTE	Chữ ký số bác sĩ (PKI). AI drug-drug interaction check. QR code xác minh đơn thuốc tại nhà thuốc.
 
12. [UCD-10] review-service
Đánh giá & Phản hồi


UCD-10	Actors: Bệnh nhân | Bác sĩ | Admin

 
Hình 12: Đánh giá & Phản hồi — review-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-10-01	Đánh giá bác sĩ sau khám	Bệnh nhân
UC-10-02	Viết nhận xét chi tiết	Bệnh nhân
UC-10-03	Chấm điểm theo tiêu chí	Bệnh nhân
UC-10-04	Xem đánh giá của mình	Bác sĩ
UC-10-05	Phản hồi đánh giá của bệnh nhân	Bác sĩ
UC-10-06	Xem xếp hạng bác sĩ	Bệnh nhân
UC-10-07	Báo cáo đánh giá không phù hợp	Bệnh nhân
UC-10-08	Kiểm duyệt đánh giá	Admin
UC-10-09	Ẩn / Xóa đánh giá vi phạm	Admin
UC-10-10	Thống kê điểm đánh giá	Admin

NOTE	Chỉ bệnh nhân có appointment COMPLETED mới được review. Tiêu chí: Chuyên môn, Thái độ, Đúng giờ, Giải thích rõ ràng.
 
13. [UCD-11] report-service
Báo cáo & Thống kê


UCD-11	Actors: Admin | Bác sĩ | Hệ thống BI

 
Hình 13: Báo cáo & Thống kê — report-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-11-01	Xem báo cáo lịch hẹn theo ngày/tuần/tháng	Admin
UC-11-02	Xem báo cáo doanh thu	Admin
UC-11-03	Xem thống kê bệnh nhân mới	Admin
UC-11-04	Xem hiệu suất bác sĩ	Admin
UC-11-05	Xuất báo cáo Excel/PDF	Admin
UC-11-06	Xem thống kê lịch khám cá nhân	Bác sĩ
UC-11-07	Xem phân tích xu hướng bệnh	Bác sĩ
UC-11-08	Xem dashboard thời gian thực	Admin
UC-11-09	Cấu hình báo cáo tự động (Schedule)	Admin
UC-11-10	Gửi báo cáo qua Email tự động	Hệ thống

NOTE	Data pipeline: PostgreSQL → Data Warehouse → BI Dashboard (Grafana/Metabase). Scheduled reports via Cron Job.
 
14. [UCD-12] admin-service
Quản trị Hệ thống


UCD-12	Actors: Admin | Super Admin

 
Hình 14: Quản trị Hệ thống — admin-service

Danh sách Use Case
UC ID	Tên Use Case	Actor chính
UC-12-01	Quản lý tài khoản Admin	Super Admin
UC-12-02	Cấu hình hệ thống	Super Admin
UC-12-03	Quản lý phân quyền (Role & Permission)	Super Admin
UC-12-04	Quản lý danh mục chuyên khoa	Admin
UC-12-05	Quản lý danh mục dịch vụ & giá	Admin
UC-12-06	Duyệt hồ sơ bác sĩ mới	Admin
UC-12-07	Xem audit log hệ thống	Admin, Super Admin
UC-12-08	Quản lý banner & thông báo hệ thống	Admin
UC-12-09	Backup & Restore dữ liệu	Super Admin
UC-12-10	Monitor health check các service	Super Admin

NOTE	Admin-service là Control Plane của toàn hệ thống. Super Admin có full access. Tất cả actions ghi immutable audit log với timestamp.

