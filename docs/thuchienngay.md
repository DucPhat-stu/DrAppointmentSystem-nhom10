# Implementation Plan: Bổ sung UC còn thiếu trong 6 Services hiện có

## 1. Tóm tắt

Bổ sung **~24 UC** còn thiếu trong 6 service hiện có (auth, user, doctor, appointment, notification, ai) so với đồ án.docx. Mỗi UC có cả **BE + FE**. Mỗi story = **1 commit**.

### Lựa chọn đã xác nhận
- **Phạm vi**: Option A — chỉ bổ sung UC thiếu trong 6 service hiện có
- **OTP/2FA**: Option A — Mock/Giả lập (OTP luôn "123456", Email chỉ log console)
- **FE/BE**: Option A — cả BE + FE cho mỗi UC

---

## 2. Phân tích ảnh hưởng (Impact Analysis)

> [!IMPORTANT]
> Kết quả kiểm tra: **KHÔNG ảnh hưởng đến dự án hiện tại** vì tất cả thay đổi đều là **additive** (thêm mới), không sửa đổi logic cũ.

| Thay đổi | Loại | Ảnh hưởng đến code cũ | Chi tiết |
|---|---|---|---|
| Thêm column `password_reset_token` vào bảng `users` | DB Migration | ❌ Không | Thêm V6 migration mới, column nullable |
| Thêm bảng `notification_preferences` | DB Migration | ❌ Không | Bảng mới hoàn toàn |
| Thêm bảng `ai_conversations`, `ai_messages` | DB Migration | ❌ Không | Bảng mới |
| Thêm endpoint `/auth/forgot-password` | API | ❌ Không | Endpoint mới, không trùng |
| Thêm endpoint `/auth/change-password` | API | ❌ Không | Endpoint mới |
| Thêm `@Scheduled` reminder | Logic | ❌ Không | Job mới, không ảnh hưởng flow cũ |
| Thêm AI endpoints mới | API | ❌ Không | Chỉ thêm, không sửa |
| Thêm FE pages/routes | Frontend | ❌ Không | Chỉ thêm route, không sửa |

### Rủi ro cần quản lý

| Rủi ro | Mức | Giảm thiểu |
|---|---|---|
| Flyway migration version conflict | 🟡 Medium | Kiểm tra version cuối cùng của mỗi service trước khi thêm |
| New dependencies (file upload) | 🟢 Low | Dùng Spring multipart có sẵn |
| AI service DB schema mới | 🟢 Low | Thêm V3+ migration |
| FE router mới | 🟢 Low | Chỉ append vào router.jsx |

---

## 3. Danh sách UC cần bổ sung (đã xác minh với code)

### 3.1 auth-service — 5 UC thiếu

| UC | Tên | Mock/Real | Effort |
|---|---|---|---|
| UC-01-03 | Đăng nhập SĐT + OTP | Mock (OTP = "123456") | 2h |
| UC-01-04 | Đăng nhập mã bác sĩ | Mock | 1h |
| UC-01-05 | 2FA (TOTP) | Mock (code = "123456") | 2h |
| UC-01-07/08 | Quên + Đặt lại mật khẩu | Mock (token log console) | 2h |
| UC-01-09 | Đổi mật khẩu | Real | 1h |

### 3.2 user-service — 4 UC thiếu

| UC | Tên | Ghi chú | Effort |
|---|---|---|---|
| UC-02-03 | Upload ảnh đại diện | Spring Multipart, lưu disk | 2h |
| UC-02-06 | QL hồ sơ chuyên môn BS | CRUD certifications | 2h |
| UC-02-07 | Cập nhật chứng chỉ hành nghề | Gộp với UC-02-06 | — |
| UC-02-09 | Tìm kiếm người dùng (Admin) | Search by name/email/role | 1h |

### 3.3 doctor-service — ✅ Hoàn chỉnh 11/11

### 3.4 appointment-service — 1 UC cần FE

| UC | Tên | Ghi chú | Effort |
|---|---|---|---|
| UC-04-07 | Từ chối lịch (BS) | ✅ BE đã có `reject()` — chỉ cần FE button | 1h |
| UC-04-10 | Nhắc lịch tự động | Scheduler + publish event | 2h |

### 3.5 notification-service — 4 UC thiếu (bỏ 2 UC phụ thuộc service chưa có)

| UC | Tên | Ghi chú | Effort |
|---|---|---|---|
| UC-05-02 | Nhắc lịch 24h trước | Consumer xử lý reminder event | 1h |
| UC-05-03 | Nhắc lịch 1h trước | Gộp với UC-05-02 | — |
| UC-05-05 | ~~TB kết quả xét nghiệm~~ | ❌ Skip — cần medical-record service | — |
| UC-05-06 | ~~TB đơn thuốc sẵn sàng~~ | ❌ Skip — cần prescription service | — |
| UC-05-09 | Cài đặt tùy chọn thông báo | Preference CRUD | 2h |
| UC-05-10 | Gửi TB hàng loạt (Broadcast) | Admin endpoint + consumer | 2h |

### 3.6 ai-service — 10 UC thiếu (bỏ 1 UC phụ thuộc medical-record)

| UC | Tên | Ghi chú | Effort |
|---|---|---|---|
| UC-07-04 | AI phân tích hình ảnh y tế | Mock — nhận file, trả kết quả mẫu | 2h |
| UC-07-05 | AI gợi ý chẩn đoán sơ bộ | Enhance existing chatbot prompt | 1h |
| UC-07-06 | AI gợi ý phác đồ điều trị | Enhance existing chatbot prompt | 1h |
| UC-07-07 | Xem lịch sử hội thoại AI | DB persistence + list API | 3h |
| UC-07-08 | Đánh giá độ chính xác AI | Feedback endpoint | 1h |
| UC-07-09 | AI gợi ý bác sĩ phù hợp | Query doctor-service + AI match | 2h |
| UC-07-10 | AI gợi ý thời gian tái khám | Based on diagnosis | 1h |
| UC-07-11 | Dự đoán thời gian chờ khám | Mock analytics | 1h |
| UC-07-12 | Phân tích xu hướng bệnh | Mock analytics dashboard | 2h |
| UC-07-13 | Cảnh báo rủi ro sức khỏe | Enhance chatbot response | 1h |
| UC-07-14 | ~~Tóm tắt hồ sơ bệnh án~~ | ❌ Skip — cần medical-record service | — |

---

## 4. Sprint Plan (5 Sprints, 24 Stories)

---

### Sprint 1: Auth Enhancements (6 stories)

| Story | UC | Tên | Scope | Commit message |
|---|---|---|---|---|
| S1.1 | UC-01-07/08 | Quên + Đặt lại mật khẩu (BE) | Migration V6 + ForgotPasswordUseCase + ResetPasswordUseCase + endpoints | `feat(auth): add forgot/reset password mock flow` |
| S1.2 | UC-01-07/08 | Quên + Đặt lại mật khẩu (FE) | ForgotPasswordPage + ResetPasswordPage + routes | `feat(frontend): add forgot/reset password pages` |
| S1.3 | UC-01-09 | Đổi mật khẩu (BE+FE) | ChangePasswordUseCase + endpoint + ProfilePage update | `feat(auth): add change password with UI` |
| S1.4 | UC-01-03 | Đăng nhập OTP mock (BE+FE) | OtpLoginUseCase + mock SMS + LoginPage OTP tab | `feat(auth): add OTP login mock flow` |
| S1.5 | UC-01-04 | Đăng nhập mã bác sĩ (BE+FE) | DoctorCodeLoginUseCase + LoginPage doctor tab | `feat(auth): add doctor code login mock` |
| S1.6 | UC-01-05 | Xác thực 2FA mock (BE+FE) | TwoFactorUseCase + setup/verify endpoints + TwoFactorPage | `feat(auth): add 2FA TOTP mock flow` |

---

### Sprint 2: User + Appointment Enhancements (5 stories)

| Story | UC | Tên | Scope | Commit message |
|---|---|---|---|---|
| S2.1 | UC-02-03 | Upload ảnh đại diện (BE+FE) | Multipart endpoint + avatar UI in ProfilePage | `feat(user): add avatar upload` |
| S2.2 | UC-02-06/07 | QL hồ sơ chuyên môn BS (BE+FE) | Certifications CRUD + DoctorProfilePage | `feat(user): add doctor professional profile management` |
| S2.3 | UC-02-09 | Tìm kiếm người dùng (BE+FE) | Search API + AdminUsersPage search bar | `feat(user): add user search for admin` |
| S2.4 | UC-04-07 | Từ chối lịch hẹn FE | Reject button in DoctorAppointmentDashboardPage | `feat(frontend): add reject appointment button for doctor` |
| S2.5 | UC-04-10 | Nhắc lịch tự động (BE) | AppointmentReminderScheduler + publish reminder events | `feat(appointment): add auto appointment reminder scheduler` |

---

### Sprint 3: Notification Enhancements (3 stories)

| Story | UC | Tên | Scope | Commit message |
|---|---|---|---|---|
| S3.1 | UC-05-02/03 | Nhắc lịch 24h + 1h (BE) | Consumer cho APPOINTMENT_REMINDER event + cron trigger | `feat(notification): add 24h and 1h appointment reminders` |
| S3.2 | UC-05-09 | Cài đặt tùy chọn TB (BE+FE) | NotificationPreference entity + CRUD API + Settings page | `feat(notification): add notification preferences` |
| S3.3 | UC-05-10 | Gửi TB hàng loạt (BE+FE) | Admin broadcast endpoint + AdminNotificationPage | `feat(notification): add admin broadcast notifications` |

---

### Sprint 4: AI Enhancements Part 1 (5 stories)

| Story | UC | Tên | Scope | Commit message |
|---|---|---|---|---|
| S4.1 | UC-07-07 | Lịch sử hội thoại AI (BE+FE) | ai_conversations + ai_messages tables + list API + ChatbotPage history | `feat(ai): add conversation history persistence` |
| S4.2 | UC-07-05/06 | AI gợi ý chẩn đoán + phác đồ (BE+FE) | Enhanced prompt + new response fields + ChatbotPage tabs | `feat(ai): add preliminary diagnosis and treatment suggestions` |
| S4.3 | UC-07-08 | Đánh giá độ chính xác AI (BE+FE) | Feedback endpoint + thumbs up/down UI | `feat(ai): add AI accuracy feedback mechanism` |
| S4.4 | UC-07-09 | AI gợi ý bác sĩ phù hợp (BE+FE) | DoctorRecommendation endpoint + UI card | `feat(ai): add AI doctor recommendation` |
| S4.5 | UC-07-04 | AI phân tích hình ảnh y tế mock (BE+FE) | Image upload endpoint + mock analysis + UI | `feat(ai): add medical image analysis mock` |

---

### Sprint 5: AI Enhancements Part 2 (5 stories)

| Story | UC | Tên | Scope | Commit message |
|---|---|---|---|---|
| S5.1 | UC-07-10 | AI gợi ý thời gian tái khám (BE+FE) | Follow-up suggestion endpoint + UI | `feat(ai): add follow-up appointment time suggestion` |
| S5.2 | UC-07-11 | Dự đoán thời gian chờ khám (BE+FE) | Wait time prediction endpoint + UI widget | `feat(ai): add wait time prediction` |
| S5.3 | UC-07-12 | Phân tích xu hướng bệnh (BE+FE) | Analytics endpoint + chart page | `feat(ai): add disease trend analysis` |
| S5.4 | UC-07-13 | Cảnh báo rủi ro sức khỏe (BE+FE) | Health risk alert endpoint + notification | `feat(ai): add health risk alerts` |
| S5.5 | — | Cleanup + final verification | Verify all 24 UCs, update docs | `chore: final UC verification and docs update` |

---

## 5. Technical Details mỗi Sprint

### Sprint 1 — Auth Service Changes

#### DB Migration V6 (auth-service)
```sql
-- V6__add_password_reset_and_otp.sql
ALTER TABLE users ADD COLUMN password_reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN password_reset_expires_at TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN phone_otp VARCHAR(6);
ALTER TABLE users ADD COLUMN phone_otp_expires_at TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN two_factor_secret VARCHAR(64);
ALTER TABLE users ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN doctor_code VARCHAR(32);
```

#### New Java Files
- `service/password/ForgotPasswordUseCase.java`
- `service/password/ResetPasswordUseCase.java`
- `service/password/ChangePasswordUseCase.java`
- `service/otp/OtpLoginUseCase.java`
- `service/otp/MockOtpSender.java`
- `service/twofa/TwoFactorSetupUseCase.java`
- `service/twofa/TwoFactorVerifyUseCase.java`
- `service/doctorcode/DoctorCodeLoginUseCase.java`
- `dto/request/ForgotPasswordRequest.java`
- `dto/request/ResetPasswordRequest.java`
- `dto/request/ChangePasswordRequest.java`
- `dto/request/OtpLoginRequest.java`
- `dto/request/OtpVerifyRequest.java`
- `dto/request/TwoFactorSetupRequest.java`
- `dto/request/DoctorCodeLoginRequest.java`

#### New FE Pages
- `pages/ForgotPasswordPage/ForgotPasswordPage.jsx`
- `pages/ResetPasswordPage/ResetPasswordPage.jsx`
- `pages/TwoFactorPage/TwoFactorPage.jsx`

---

### Sprint 2 — User + Appointment Changes

#### DB Migration V7 (user-service)
```sql
-- V7__add_avatar_and_certifications.sql
ALTER TABLE user_profiles ADD COLUMN avatar_url VARCHAR(512);

CREATE TABLE IF NOT EXISTS doctor_certifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_profiles(user_id),
    name VARCHAR(255) NOT NULL,
    issuing_authority VARCHAR(255),
    issue_date DATE,
    expiry_date DATE,
    document_url VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Appointment Reminder (appointment-service)
- `scheduler/AppointmentReminderScheduler.java` — `@Scheduled(cron = "0 */15 * * * *")` scan confirmed appointments 24h/1h away
- Publish `APPOINTMENT_REMINDER_24H` and `APPOINTMENT_REMINDER_1H` events to RabbitMQ

---

### Sprint 3 — Notification Changes

#### DB Migration V5 (notification-service)
```sql
-- V5__add_notification_preferences.sql
CREATE TABLE IF NOT EXISTS notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reminder_24h BOOLEAN NOT NULL DEFAULT TRUE,
    reminder_1h BOOLEAN NOT NULL DEFAULT TRUE,
    appointment_updates BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

### Sprint 4 & 5 — AI Service Changes

#### DB Migration V3 (ai-service)
```sql
-- V3__add_conversations_and_feedback.sql
CREATE TABLE IF NOT EXISTS ai_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES ai_conversations(id),
    role VARCHAR(20) NOT NULL, -- USER or ASSISTANT
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES ai_messages(id),
    user_id UUID NOT NULL,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_conversations_user ON ai_conversations(user_id);
CREATE INDEX idx_ai_messages_conversation ON ai_messages(conversation_id);
```

---

## 6. Verification Plan

### Automated Tests
- Build tất cả 6 service: `mvn clean compile -pl services/auth-service,services/user-service,services/doctor-service,services/appointment-service,services/notification-service,services/ai-service`
- Run existing tests: `mvn test` — đảm bảo không break
- FE build: `npm run build` — no errors

### Manual Verification
- Chạy Docker Compose + tất cả services
- Test từng UC mới qua browser
- Verify git log có đúng 24 commits

### Checklist per Story
1. ✅ BE endpoint hoạt động (Postman)
2. ✅ FE page render đúng
3. ✅ Không break existing tests
4. ✅ Git commit với message đúng format

---

## 7. Flyway Version Map (hiện tại → sẽ thêm)

| Service | Version hiện tại | Sẽ thêm |
|---|---|---|
| auth-service | V5 | V6 (password reset, OTP, 2FA, doctor code) |
| user-service | V6 | V7 (avatar, certifications) |
| doctor-service | V9 | Không thêm |
| appointment-service | V10 | V11 (reminder tracking) |
| notification-service | V4 | V5 (preferences) |
| ai-service | V2 | V3 (conversations, messages, feedback) |

---

## 8. Tổng kết

| Metric | Giá trị |
|---|---|
| Tổng stories | **24** |
| Tổng sprints | **5** |
| UC bổ sung | **24** (skip 3 UC phụ thuộc service chưa có) |
| Estimated time | **~35-40 giờ** |
| Risk to existing code | **THẤP** — tất cả additive |
| Services bị ảnh hưởng | 5/6 (doctor-service không cần thay đổi) |
