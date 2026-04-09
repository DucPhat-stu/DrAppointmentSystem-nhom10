# TESTING - Healthcare Platform MVP

## 1. Muc tieu
- Chot chien luoc test cho MVP theo nhieu tang.
- Lay Postman lam trung tam cho API/system testing.
- Ket hop unit, integration va smoke test de giam loi contract, security va race condition.

## 2. Testing pyramid cua du an

| Tang test | Cong cu | Muc dich |
| --- | --- | --- |
| Unit test | JUnit 5 + Mockito | Test business rule, validation, mapper, guard condition |
| Slice/Integration test | Spring Boot Test + Testcontainers | Test repository, security config, REST controller, RabbitMQ/Postgres/Redis integration |
| API test | Postman | Test endpoint theo service, test auth flow, role matrix, regression |
| System smoke test | Docker Compose + Postman | Test happy path end-to-end tren local stack |

## 3. Moi truong test local
- Ha tang local chay bang Docker Compose: `postgres`, `redis`, `rabbitmq`.
- Cac service MVP chay bang Maven de debug nhanh.
- Frontend chay bang Vite va goi API Gateway/local proxy.
- Moi truong Postman can co bien:
  - `baseUrlGateway`
  - `patientEmail`
  - `patientPassword`
  - `doctorEmail`
  - `doctorPassword`
  - `accessTokenPatient`
  - `refreshTokenPatient`
  - `accessTokenDoctor`
  - `refreshTokenDoctor`
  - `appointmentId`
  - `slotId`

## 4. Seed data toi thieu

| Doi tuong | Gia tri mau |
| --- | --- |
| Patient | `patient01@healthcare.local / Patient@123` |
| Doctor | `doctor01@healthcare.local / Doctor@123` |
| Admin | `admin01@healthcare.local / Admin@123` |
| Doctor profile | Chuyen khoa Noi tong quat, 5 nam kinh nghiem |
| TimeSlot | It nhat 3 slot `AVAILABLE` trong 2 ngay toi |

- Seed data chi dung cho local/test; khong dua vao tai lieu van hanh production.

## 5. Cau truc Postman de xuat

```text
postman/
  environments/
    healthcare-local.postman_environment.json
  collections/
    00-auth.postman_collection.json
    01-user.postman_collection.json
    02-doctor.postman_collection.json
    03-appointment.postman_collection.json
    04-notification-smoke.postman_collection.json
    99-e2e-smoke.postman_collection.json
```

### 5.1 Noi dung tung collection
- `00-auth`: register, login, refresh token, logout, negative token cases.
- `01-user`: get profile, update profile, list doctors, doctor detail.
- `02-doctor`: create slot, list slot, invalid overlap slot, doctor view appointments.
- `03-appointment`: create appointment, get detail, cancel, reschedule, doctor confirm, duplicate booking conflict.
- `04-notification-smoke`: list notifications, mark as read, verify side effect sau event.
- `99-e2e-smoke`: flow patient login -> browse doctor -> book -> doctor confirm -> patient check notification.

### 5.2 Cac token hien thi trong Postman
- Login response tra token ro rang trong `data.accessToken` va `data.refreshToken`.
- Mau response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "<jwt-access-token>",
    "refreshToken": "<refresh-token>",
    "expiresInSeconds": 900,
    "role": "PATIENT"
  }
}
```

- Postman Tests script de luu token:

```javascript
const body = pm.response.json();
pm.environment.set("accessTokenPatient", body.data.accessToken);
pm.environment.set("refreshTokenPatient", body.data.refreshToken);
```

- Khi test API can JWT, dat header:

```text
Authorization: Bearer {{accessTokenPatient}}
```

## 6. Test case bat buoc

### 6.1 Auth va security
- Dang ky thanh cong voi email moi.
- Dang ky that bai voi email trung.
- Dang nhap thanh cong tra ve access token va refresh token.
- Refresh token hop le sinh access token moi.
- Request khong co JWT bi tra `UNAUTHORIZED`.
- Patient goi endpoint doctor-only bi tra `INSUFFICIENT_PERMISSIONS`.

### 6.2 User va doctor directory
- Xem profile cua minh thanh cong.
- Cap nhat profile voi du lieu hop le thanh cong.
- Doi tuong gui payload sai format bi tra `VALIDATION_ERROR`.
- Danh sach bac si tra ve dung cac field cong khai da chot trong BA.

### 6.3 Doctor va timeslot
- Bac si tao slot hop le thanh cong.
- Tao slot trung gio bi tra `CONFLICT`.
- Tao slot trong qua khu bi tra `VALIDATION_ERROR`.
- Benh nhan xem duoc slot `AVAILABLE` cua bac si.

### 6.4 Appointment
- Benh nhan dat lich thanh cong voi slot `AVAILABLE`.
- Hai request dat cung mot slot chi co mot request thanh cong; request con lai bi `CONFLICT`.
- Bac si xac nhan lich `PENDING` thanh `CONFIRMED`.
- Benh nhan huy lich va slot tro lai `AVAILABLE`.
- Benh nhan doi lich lam appointment cu thanh `RESCHEDULED` va tao appointment moi `PENDING`.

### 6.5 Notification
- Sau `appointment.created`, notification duoc tao cho bac si va benh nhan.
- Sau `appointment.confirmed`, benh nhan nhan notification xac nhan.
- Sau `appointment.cancelled`, notification huy lich duoc tao cho doi tuong lien quan.
- Khi message duoc retry, notification khong bi duplicate neu event id da xu ly.

## 7. Unit va integration test can co trong Maven

### 7.1 Unit test
- `auth-service`: password verification, token issue rule, role guard.
- `doctor-service`: overlap slot rule, slot status transition.
- `appointment-service`: booking rule, cancel/reschedule rule, idempotent event publish guard.
- `notification-service`: event-to-template mapping, duplicate event protection.

### 7.2 Integration test
- Dung `Spring Boot Test` + `Testcontainers` cho PostgreSQL.
- Dung Redis container khi can test cache behavior.
- Dung RabbitMQ container khi can test publish/consume event.
- Test controller level cho auth header, forwarded identity va response envelope.

## 8. Kich ban smoke test end-to-end
1. Khoi dong infrastructure bang Docker Compose.
2. Chay `auth-service`, `user-service`, `doctor-service`, `appointment-service`, `notification-service`.
3. Patient login va lay JWT.
4. Patient goi danh sach bac si va lay `doctorId`.
5. Patient goi danh sach slot trong va lay `slotId`.
6. Patient dat lich, nhan `appointmentId`.
7. Doctor login va xac nhan lich.
8. Patient kiem tra notification va chi tiet appointment.

## 9. Quy uoc quan sat ket qua test
- Moi API test can assert status code, `success`, `message`, `errorCode` khi co loi, va field nghiep vu chinh trong `data`.
- Moi integration test phai assert thay doi state trong DB hoac side effect thong diep/event.
- Smoke test phai luu lai log request quan trong va request id de trace.

## 10. Lenh quy trinh can ghi nho

```powershell
cd main/docker
docker compose up -d postgres redis rabbitmq
```

```powershell
cd main/backend
mvn test
```

```powershell
cd main/backend
mvn -pl services/auth-service spring-boot:run
```

- Trong giai doan nay, cac lenh tren duoc tai lieu hoa de doi du an thuc thi sau; chua coi la tieu chi phai run ngay trong lan cap nhat docs nay.

## 11. Dieu khong bo qua trong regression
- Regression auth sau moi thay doi security filter.
- Regression booking sau moi thay doi `TimeSlot` hoac locking strategy.
- Regression notification sau moi thay doi routing key RabbitMQ.
- Regression frontend form sau moi thay doi contract response/error.
