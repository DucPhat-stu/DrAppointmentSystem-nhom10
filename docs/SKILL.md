# Healthcare Platform Skillbook

## 1. Muc tieu tai lieu
- Dung tai lieu nay nhu handbook noi bo cho giai doan xay dung MVP cua Healthcare Platform.
- Bam theo phan tich nghiep vu trong `idea.md` va luong ky thuat trong `sequence.md`.
- Chi tap trung vao 5 service MVP: `auth-service`, `user-service`, `doctor-service`, `appointment-service`, `notification-service`, cung frontend booking flow.

## 2. Tech Baseline

| Thanh phan | Lua chon |
| --- | --- |
| Backend | Java 21, Spring Boot 3.x, Maven multi-module |
| Database | PostgreSQL |
| Cache | Redis |
| Messaging | RabbitMQ |
| Frontend | React + Vite, HTML5, CSS3, JavaScript |
| UI styling | CSS Modules, khong dung CSS framework o MVP |
| Auth | JWT access token + refresh token |
| API style | REST JSON |
| Test | JUnit 5, Mockito, Spring Boot Test, Testcontainers, Postman |
| Runtime local | Docker Compose + Maven + Node.js |

## 3. Nguyen tac lam viec
- `idea.md` la nguon goc nghiep vu; `sequence.md` la nguon goc cho interaction va event flow.
- Moi thay doi contract API, enum, business rule hoac event phai cap nhat dong thoi `SKILL.md`, `BA.md`, `TESTING.md`.
- Cac task moi phai doi chieu them voi `SOLID_GUIDELINES.md` de tranh day domain rule vao `shared/*` va tranh lam shared module thanh application layer.
- Uu tien hoan thanh MVP end-to-end truoc khi mo rong sang 7 service con lai.
- Giu service doc lap, database tach rieng theo service, chia se thong tin qua REST va RabbitMQ.
- Khong dua AI, payment, prescription, report, admin dashboard day du vao sprint MVP; chi mo khoa interface neu luong hien tai can stub.


## 4. Cau truc repo muc tieu

```text
main/
  backend/
    pom.xml
    shared/
      common/
      security-contract/
      api-contract/
    services/
      auth-service/
      user-service/
      doctor-service/
      appointment-service/
      notification-service/
  docker/
    compose.yml
    .env.example
  frontend/
    package.json
    vite.config.js
    src/
      app/
      routes/
      pages/
      components/
      services/
      hooks/
      styles/
```

## 5. Backend Workflow

### 5.1 Thu tu dung service
1. Dung `auth-service` de khoa user identity, JWT, refresh token, RBAC.
2. Dung `user-service` de quan ly ho so benh nhan va ho so bac si o muc co ban.
3. Dung `doctor-service` de quan ly lich lam viec va `TimeSlot`.
4. Dung `appointment-service` de dat lich, doi lich, huy lich, dong bo trang thai slot.
5. Dung `notification-service` de subscribe event tu `appointment-service` va gui thong bao.

### 5.2 Chuan module Spring Boot
- Moi service la mot Maven module doc lap, co `spring-boot-starter-parent` duoc ke thua tu parent `backend/pom.xml`.
- Package ben trong moi service uu tien tach ro `config`, `controller`, `dto/request`, `dto/response`, `entity`, `exception`, `repository`, `security`, `service`.
- Ben trong `service` duoc phep tach tiep theo use case nhu `login`, `register`, `token`; tranh nhoi mot package `application` qua to.
- DTO request/response dung Java `record`; mapping uu tien viet tay de MVP ro rang, khong them mapper library.
- Validation dung `jakarta.validation` tai request layer; khong de business rule phuc tap nam trong controller.
- Migration dung Flyway, luu tai `src/main/resources/db/migration`.
- Config environment duoc doc tu bien moi truong; khong hardcode secret vao code.
- Bat `Spring Actuator` cho health check va readiness.

### 5.3 Chuan contract API
- Tat ca request tu client di qua API Gateway.
- Client gui `Authorization: Bearer <jwt>`.
- Gateway sau khi verify JWT se forward `X-User-Id`, `X-User-Role`; downstream service khong tin headers tu internet public.
- Response thanh cong dung envelope:

```json
{
  "success": true,
  "message": "Appointment created",
  "data": {},
  "meta": {
    "requestId": "req-123",
    "timestamp": "2026-04-06T12:00:00Z"
  }
}
```

- Response loi dung envelope:

```json
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [],
  "meta": {
    "requestId": "req-123",
    "timestamp": "2026-04-06T12:00:00Z"
  }
}
```

- Ma loi dung xuyen suot MVP: `VALIDATION_ERROR`, `UNAUTHORIZED`, `INSUFFICIENT_PERMISSIONS`, `RESOURCE_NOT_FOUND`, `CONFLICT`, `SERVICE_UNAVAILABLE`.
- Role MVP: `PATIENT`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN`.
- Enum nghiep vu co dinh:
  - `AppointmentStatus = PENDING | CONFIRMED | COMPLETED | CANCELLED | RESCHEDULED`
  - `TimeSlotStatus = AVAILABLE | BOOKED | BLOCKED`

### 5.4 Chuan auth va bao mat
- Access token het han sau 15 phut; refresh token het han sau 7 ngay.
- Password hash dung BCrypt.
- `auth-service` la nguon goc phat hanh JWT va quan ly refresh token.
- MVP local cho phep self-register tao ngay user `PATIENT` o trang thai `ACTIVE`.
- Cac use case bi loai khoi scope runtime MVP cua `auth-service`: OTP/email verification, login bang phone OTP, doctor-code login, 2FA, SMS/email integration, force-logout blacklist.
- `ADMIN` va `SUPER_ADMIN` chi duoc giu o muc reserved contract, khong phai flow thuc thi trong local MVP.
- Endpoint noi bo giua service phai co timeout va log `requestId`.

### 5.5 Trach nhiem tung service MVP

| Service | Trach nhiem chinh | REST chinh | Event xu ly |
| --- | --- | --- | --- |
| `auth-service` | Dang ky benh nhan, dang nhap patient/doctor, refresh token, logout, JWT role mapping toi thieu | `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout` | Khong publish event trong MVP |
| `user-service` | Ho so nguoi dung, danh sach bac si, chi tiet bac si | `/users/me`, `/users/me/profile`, `/doctors`, `/doctors/{id}` | Khong publish event trong MVP |
| `doctor-service` | Quan ly lich lam viec, slot trong, block slot | `/doctors/{id}/timeslots`, `/doctors/me/timeslots` | Co the phat event bo sung o phase sau |
| `appointment-service` | Tao, doi, huy, xem chi tiet lich hen | `/appointments`, `/appointments/{id}`, `/appointments/{id}/cancel`, `/appointments/{id}/reschedule`, `/appointments/{id}/confirm` | Publish `appointment.created`, `appointment.confirmed`, `appointment.cancelled`, `appointment.reminder.due` |
| `notification-service` | Subscribe event, render template, gui email/push | `/notifications`, `/notifications/{id}/read` | Subscribe toan bo event appointment MVP |

### 5.6 Quy tac dat lich quan trong
- Benh nhan chi duoc dat `TimeSlot` dang `AVAILABLE`.
- Mot `TimeSlot` chi duoc gan voi mot lich hen active tai cung thoi diem.
- Khi dat lich thanh cong, `appointment-service` khoa slot va chuyen slot sang `BOOKED`.
- Khi huy lich, slot tro lai `AVAILABLE`.
- Khi doi lich, appointment cu duoc danh dau `RESCHEDULED`, slot cu tra lai `AVAILABLE`, sau do tao appointment moi o trang thai `PENDING`.
- `doctor-service` la noi so huu du lieu `TimeSlot`; `appointment-service` khong tu sua slot truc tiep trong DB cua service khac.

### 5.7 RabbitMQ va workflow async
- Exchange mac dinh cho MVP: `appointment.events`.
- Routing key dung ten event:
  - `appointment.created`
  - `appointment.confirmed`
  - `appointment.cancelled`
  - `appointment.reminder.due`
- `notification-service` subscribe cac routing key tren va retry 3 lan voi exponential backoff.
- Scheduler nhac lich chay trong `appointment-service` va publish `appointment.reminder.due` cho lich `CONFIRMED`.

### 5.8 Local run quy uoc

```powershell
cd main/docker
docker compose up -d postgres redis rabbitmq
```

```powershell
cd main/backend
mvn -pl services/auth-service spring-boot:run
mvn -pl services/user-service spring-boot:run
mvn -pl services/doctor-service spring-boot:run
mvn -pl services/appointment-service spring-boot:run
mvn -pl services/notification-service spring-boot:run
```

- Moi service doc port tu `application.yml` + env file local.
- Docker Compose chi host infrastructure; service MVP chay bang Maven trong giai doan dau de debug nhanh.

### 5.9 Definition of Done cho backend task
- Contract API va validation ro rang.
- Flyway migration tao bang/chi muc can thiet.
- Unit test cho service rule quan trong.
- Integration test cho repository, security hoac message flow can thiet.
- Health endpoint hoat dong.
- Postman request tuong ung duoc cap nhat trong `TESTING.md`.

## 6. Frontend Workflow

### 6.1 Kien truc frontend
- Dung React + Vite de giu DX tot hon JS thuan trong khi van dung HTML5, CSS3, JavaScript.
- Khong dung SSR, khong dung Tailwind, khong dua state management library lon vao MVP.
- Dung `React Router` cho route, `Context` cho auth session, `fetch` wrapper rieng trong `src/services`.

### 6.2 Cau truc `src`

```text
src/
  app/
    App.jsx
    router.jsx
    providers.jsx
  pages/
    LoginPage/
    DoctorListPage/
    DoctorDetailPage/
    BookAppointmentPage/
    AppointmentDetailPage/
    ProfilePage/
    NotificationPage/
  components/
    layout/
    forms/
    doctor/
    appointment/
    feedback/
  services/
    httpClient.js
    authService.js
    doctorService.js
    appointmentService.js
    notificationService.js
  hooks/
    useAuth.js
    useApiStatus.js
  styles/
    variables.css
```

### 6.3 Route MVP

| Route | Man hinh | Muc dich |
| --- | --- | --- |
| `/login` | Dang nhap | Nhan JWT va khoi tao session |
| `/doctors` | Danh sach bac si | Tim kiem, loc, xem danh sach |
| `/doctors/:doctorId` | Chi tiet bac si | Xem profile va slot trong |
| `/appointments/book` | Dat lich | Chon bac si, ngay, slot, ly do kham |
| `/appointments/:appointmentId` | Chi tiet lich hen | Xem trang thai, huy, doi lich |
| `/profile` | Ho so benh nhan | Xem va cap nhat thong tin ca nhan |
| `/notifications` | Thong bao | Xem thong bao appointment |

### 6.4 Quy tac goi API va quan ly state
- Moi request API di qua `httpClient.js`; khong goi `fetch` truc tiep trong component.
- JWT luu o memory state + `sessionStorage` cho MVP local; refresh flow duoc xu ly trong `authService`.
- State theo trang uu tien `useState`/`useReducer`; chi dua vao `Context` cho auth va user session.
- Form dung controlled component va helper validation; hien thi loi field-level ngay tai man hinh.
- Data loading can co 4 state ro rang: `idle`, `loading`, `success`, `error`.

### 6.5 Quy tac UI
- Dung CSS Modules cho page/component; bien mau, spacing, typography dat trong `styles/variables.css`.
- Thiet ke uu tien booking flow nhanh, form ro rang, co feedback sau moi thao tac.
- Tren mobile, doctor card, slot picker va appointment summary phai doc duoc khong can zoom.
- Error UI phai phan biet ro loi validation, loi quyen truy cap va loi server.

### 6.6 Definition of Done cho frontend task
- Route hoat dong va co guard neu can dang nhap.
- Man hinh render dung tren desktop va mobile.
- Form validation, loading state, empty state va error state day du.
- API service module duoc tach rieng, khong de logic HTTP trong JSX.
- CSS Modules khong co style global khong kiem soat, tru `variables.css` va reset.

## 7. Nguyen tac khi mo rong sau MVP
- Service moi phai tai su dung contract response, error code va cach forward identity hien tai.
- Service phase sau duoc them tung buoc: `medical-record`, `payment`, `prescription`, `review`, `report`, `admin`, `ai`.
- Neu can stub cho service phase sau, uu tien stub tai gateway hoac mock client thay vi chen logic tam vao service MVP.
