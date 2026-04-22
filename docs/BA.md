# BA MVP - Healthcare Platform

## 1. Muc dich
- Chuyen phan tich tong quan trong `idea.md` thanh pham vi nghiep vu co the trien khai ngay cho MVP.
- Tap trung vao luong dat lich kham cua benh nhan va xac nhan lich cua bac si.

## 2. Actor va muc tieu

| Actor | Role | Muc tieu trong MVP |
| --- | --- | --- |
| Benh nhan | `PATIENT` | Dang ky, dang nhap, xem bac si, xem slot trong, dat lich, doi/huy lich, xem thong bao, cap nhat ho so |
| Bac si | `DOCTOR` | Dang nhap, quan ly slot kham, xem lich hen, xac nhan lich, huy lich phia bac si |
| Quan tri vien | `ADMIN` | Khong co UI MVP, chi giu contract role va du lieu nen |
| He thong | `SYSTEM` | Phat event, gui nhac lich, dong bo notification |

## 3. Pham vi MVP

### 3.1 Trong pham vi
- Dang ky va dang nhap bang email/mat khau.
- Quan ly profile benh nhan va ho so bac si co ban.
- Hien thi danh sach bac si va thong tin chi tiet.
- Quan ly `TimeSlot` cua bac si.
- Dat lich, xem lich, doi lich, huy lich.
- Gui notification khi tao, xac nhan, huy va nhac lich.
- Frontend cho luong patient booking.

### 3.2 Ngoai pham vi MVP
- Ho so benh an.
- Thanh toan online.
- Ke don thuoc.
- Danh gia bac si.
- Bao cao BI.
- Quan tri he thong day du.
- AI recommendation va chatbot.

## 4. Use case uu tien

| Nhom | Use case uu tien |
| --- | --- |
| Auth | Dang ky, dang nhap, refresh token, dang xuat |
| User | Xem/cap nhat profile, xem danh sach bac si, xem chi tiet bac si |
| Doctor | Tao slot, sua slot, khoa slot, xem lich hen, xac nhan lich |
| Appointment | Dat lich, xem chi tiet, huy lich, doi lich |
| Notification | Gui thong bao tao lich, xac nhan lich, huy lich, nhac lich |

## 5. User journey chinh
1. Benh nhan dang ky hoac dang nhap vao he thong.
2. Benh nhan mo danh sach bac si, loc theo chuyen khoa hoac xem chi tiet.
3. Benh nhan chon ngay kham va slot dang `AVAILABLE`.
4. He thong tao appointment o trang thai `PENDING` va khoa slot.
5. `appointment-service` phat `appointment.created`; `notification-service` gui thong bao.
6. Bac si dang nhap, xem lich hen va xac nhan.
7. He thong chuyen appointment sang `CONFIRMED`, phat `appointment.confirmed`, gui thong bao cho benh nhan.
8. Truoc gio kham, scheduler phat `appointment.reminder.due`.

## 6. Domain entity MVP

| Entity | Thuoc tinh chinh | Ghi chu nghiep vu |
| --- | --- | --- |
| User | `id`, `email`, `passwordHash`, `role`, `status`, `fullName`, `phone` | Dung cho benh nhan va bac si |
| PatientProfile | `userId`, `dob`, `gender`, `address`, `emergencyContact` | Thuoc `user-service` |
| DoctorProfile | `userId`, `specialty`, `experienceYears`, `bio`, `licenseCode`, `avatarUrl` | Co the duoc hien thi cong khai |
| TimeSlot | `id`, `doctorId`, `date`, `startTime`, `endTime`, `status` | Thuoc `doctor-service` |
| Appointment | `id`, `patientId`, `doctorId`, `slotId`, `reason`, `status`, `createdAt`, `rescheduledFrom` | Thuoc `appointment-service` |
| Notification | `id`, `recipientId`, `type`, `title`, `content`, `read`, `createdAt` | Thuoc `notification-service` |
| RefreshToken | `id`, `userId`, `token`, `expiresAt`, `revoked` | Thuoc `auth-service` |

## 7. Business rules can khoa
- `email` la duy nhat trong he thong.
- Self-register trong MVP local chi tao duoc user role `PATIENT` va kich hoat ngay o trang thai `ACTIVE`.
- `auth-service` MVP chi giu 4 use case thuc thi: `register`, `login`, `refresh`, `logout`.
- Cac flow `verify-email`, OTP qua phone, `doctor_code`, 2FA, SMS/email verification va admin auth flow duoc dua ra khoi pham vi runtime local.
- Chi user co `status = ACTIVE` moi dang nhap duoc.
- Request `login` co the gui them `actor = PATIENT | DOCTOR` de rang buoc dung actor dang truy cap; neu bo trong `actor` thi flow van tuong thich nguoc theo email/password.
- Chi bac si duoc tao va quan ly `TimeSlot` cua chinh minh.
- Slot khong duoc trung gio voi slot khac cua cung bac si.
- Chi slot `AVAILABLE` moi duoc dat lich.
- Mot appointment moi duoc tao o trang thai `PENDING`.
- Bac si co the xac nhan appointment `PENDING`; sau xac nhan, trang thai chuyen `CONFIRMED`.
- Benh nhan co the huy appointment khi appointment chua `COMPLETED` va chua bi `CANCELLED`.
- Khi benh nhan doi lich, appointment cu chuyen `RESCHEDULED`, slot cu giai phong, appointment moi duoc tao lai o `PENDING`.
- `notification-service` khong tao su kien nghiep vu moi; service nay chi xu ly event da phat tu `appointment-service`.

## 8. Ma tran quyen MVP

| Chuc nang | PATIENT | DOCTOR | ADMIN | SUPER_ADMIN |
| --- | --- | --- | --- | --- |
| Dang ky/dang nhap | Co | Co | Co | Co |
| Xem profile cua minh | Co | Co | Co | Co |
| Cap nhat profile cua minh | Co | Co | Co | Co |
| Xem danh sach bac si | Co | Co | Co | Co |
| Tao/sua/xoa slot | Khong | Co | Khong | Khong |
| Dat lich | Co | Khong | Khong | Khong |
| Xac nhan lich | Khong | Co | Khong | Khong |
| Huy lich benh nhan | Co | Khong | Khong | Khong |
| Huy lich phia bac si | Khong | Co | Khong | Khong |
| Xem thong bao cua minh | Co | Co | Khong | Khong |

## 9. Acceptance criteria theo module

### 9.1 Auth
- Dang ky tao duoc user moi voi role `PATIENT`.
- User vua dang ky co the dang nhap ngay trong local MVP ma khong can buoc verify email.
- Dang nhap su dung email/password cho `PATIENT` va `DOCTOR`; khong mo them luong OTP/2FA o MVP.
- Khi client gui `actor`, credentials phai khop dung actor do; credentials hop le nhung sai actor bi tra `UNAUTHORIZED`.
- Dang nhap tra ve access token va refresh token hop le.
- Request khong co token hoac token sai bi tra `UNAUTHORIZED`.
- User dang nhap role `PATIENT` khong duoc goi endpoint chi danh cho `DOCTOR`.

### 9.2 User
- Benh nhan xem va cap nhat profile cua minh.
- Benh nhan xem duoc danh sach bac si va chi tiet bac si.
- Du lieu hien thi cua bac si toi thieu gom ho ten, chuyen khoa, mo ta ngan, nam kinh nghiem.

### 9.3 Doctor
- Bac si tao duoc slot hop le trong tuong lai.
- Slot bi tu choi neu trung thoi gian hoac sai dinh dang.
- Bac si xem duoc danh sach appointment lien quan den minh.

### 9.4 Appointment
- Dat lich thanh cong chi khi slot dang `AVAILABLE`.
- He thong chong duoc truong hop hai request dat cung mot slot.
- Huy lich giai phong slot.
- Doi lich tao duoc appointment moi va luu lien ket `rescheduledFrom`.

### 9.5 Notification
- Tao appointment se sinh notification cho benh nhan va bac si.
- Xac nhan appointment se sinh notification cho benh nhan.
- Huy appointment se sinh notification cho doi tuong lien quan.
- Event retry khong duoc tao duplicate notification neu message bi xu ly lai.

### 9.6 Frontend
- Benh nhan dang nhap thanh cong va xem duoc danh sach bac si.
- Benh nhan dat lich tu giao dien, sau do xem duoc trang thai lich hen.
- Giao dien hien thi ro loading, empty va error state.

## 10. Product backlog theo phase

| Phase | Hang muc |
| --- | --- |
| Phase 0 | Chot repo structure, parent Maven, Docker Compose, convention env, convention API |
| Phase 1 | Auth + User + Postman collection nen |
| Phase 2 | Doctor + Appointment + optimistic locking |
| Phase 3 | Notification + frontend booking flow |
| Phase 4 | Hoan thien test matrix, smoke test, hardening docs |

## 11. Future scope sau MVP
- `medical-record-service`: luu SOAP note, audit log, tai lieu y te.
- `payment-service`: thu phi kham, webhook, refund.
- `prescription-service`: don thuoc, ky so, kiem tra tuong tac.
- `review-service`: danh gia sau kham.
- `report-service`: dashboard va export bao cao.
- `admin-service`: control plane, health monitor, backup.
- `ai-service`: recommendation, chatbot, tong hop du lieu.
