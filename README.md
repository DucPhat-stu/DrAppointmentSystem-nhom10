# Healthcare Platform MVP

Repository này là MVP nền tảng cho hệ thống healthcare, gồm backend microservices, frontend React/Vite và hạ tầng local bằng Docker.

- `backend/` là Maven multi-module gồm shared modules và 6 services.
- `docker/compose.yml` dựng PostgreSQL, Redis và RabbitMQ cho local development.
- `frontend/` là React + Vite shell cho luồng auth/booking.
- `auth-service` có API `register`, `login`, `refresh`, `logout`.
- `user-service`, `doctor-service`, `appointment-service`, `notification-service` có foundation endpoints và các phần nghiệp vụ theo module.
- `ai-service` có API AI symptom assistant, structured prompt, prompt template management và Gemini health check.

## Repo Structure

```text
backend/
  pom.xml
  run-service.ps1
  run-service.cmd
  start-all.ps1
  shared/
    api-contract/
    common/
    security-contract/
  services/
    auth-service/
    user-service/
    doctor-service/
    appointment-service/
    notification-service/
    ai-service/
docker/
  compose.yml
  .env.example
  postgres/
frontend/
postman/
docs/
```

## Prerequisites

Máy local cần có:

- JDK `21`
- Maven `3.9+` hoặc IntelliJ bundled Maven
- Docker Desktop
- Node.js `18+`
- SQL Workbench/J nếu muốn mở database bằng GUI SQL client

Lưu ý:

- Chạy backend từ thư mục `backend/` bằng script `run-service.ps1` hoặc `run-service.cmd`.
- Không cần đứng trong từng service module để chạy riêng; script tự gọi Maven với `-pl services/<service> -am`.
- Nếu `mvn` không có trong `PATH`, script sẽ cố tìm Maven bundled trong IntelliJ. Có thể truyền `-MavenCommand` nếu cần.
- Đợi `postgres`, `redis`, `rabbitmq` healthy trước khi chạy service để tránh lỗi kết nối lúc Spring Boot/Flyway khởi động.

## Start Local Infrastructure

Chạy toàn bộ hạ tầng:

```powershell
cd docker
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose up -d
docker compose ps
```

Hoặc từ thư mục `backend/`, chạy hạ tầng và tạo đủ database bằng script:

```powershell
cd backend
.\start-all.ps1 -InfraOnly
```

PostgreSQL local có 6 database:

- `auth_db`
- `user_db`
- `doctor_db`
- `appointment_db`
- `notification_db`
- `ai_db`

Giá trị mặc định trong `docker/.env.example`:

- `POSTGRES_USER=healthcare`
- `POSTGRES_PASSWORD=healthcare`
- `POSTGRES_PORT=5432`
- `REDIS_PORT=6379`
- `RABBITMQ_USER=healthcare`
- `RABBITMQ_PASSWORD=healthcare`
- `RABBITMQ_PORT=5673`
- `RABBITMQ_MANAGEMENT_PORT=15673`

RabbitMQ management UI:

- URL: `http://localhost:15673`
- Username: `healthcare`
- Password: `healthcare`

## Open DB With SQL Workbench/J

Sau khi PostgreSQL container đã chạy, tạo connection trong SQL Workbench/J:

| Field | Value |
| --- | --- |
| Driver | PostgreSQL |
| URL | `jdbc:postgresql://localhost:5432/auth_db` |
| Username | `healthcare` |
| Password | `healthcare` |
| Autocommit | Bật hoặc để mặc định |

Đổi database trong URL theo service cần xem:

| Service | Database URL |
| --- | --- |
| `auth-service` | `jdbc:postgresql://localhost:5432/auth_db` |
| `user-service` | `jdbc:postgresql://localhost:5432/user_db` |
| `doctor-service` | `jdbc:postgresql://localhost:5432/doctor_db` |
| `appointment-service` | `jdbc:postgresql://localhost:5432/appointment_db` |
| `notification-service` | `jdbc:postgresql://localhost:5432/notification_db` |
| `ai-service` | `jdbc:postgresql://localhost:5432/ai_db` |

Nếu SQL Workbench/J chưa có PostgreSQL driver:

1. Tải PostgreSQL JDBC driver tại `https://jdbc.postgresql.org/download/`.
2. Trong SQL Workbench/J, mở `Manage Drivers`.
3. Chọn hoặc tạo driver `PostgreSQL`.
4. Thêm file `.jar` vừa tải vào driver library.
5. Lưu driver rồi reconnect bằng URL ở bảng trên.

Smoke query:

```sql
select current_database(), current_user;
select table_schema, table_name
from information_schema.tables
where table_schema = 'public'
order by table_name;
```

## Service Matrix

| Service | Default port | Database | Extra infra | Scope |
| --- | --- | --- | --- | --- |
| `auth-service` | `8086` | `auth_db` | Postgres | Auth API + seed data |
| `user-service` | `8082` | `user_db` | Postgres | User/profile APIs |
| `doctor-service` | `8083` | `doctor_db` | Postgres | Doctor/schedule APIs |
| `appointment-service` | `8084` | `appointment_db` | Postgres + Redis + RabbitMQ | Appointment APIs |
| `notification-service` | `8085` | `notification_db` | Postgres + RabbitMQ | Notification APIs |
| `ai-service` | `8087` | `ai_db` | Postgres + Gemini API | AI symptom assistant + prompt templates |

Tất cả service expose:

- `GET /actuator/health`
- `GET /api/v1/foundation/ping`

## Run All Backend Services

Từ thư mục `backend/`, chạy toàn bộ hạ tầng và mở 6 cửa sổ PowerShell cho 6 services:

```powershell
cd backend
.\start-all.ps1
```

Chạy clean build cho tất cả services:

```powershell
.\start-all.ps1 -Clean
```

Nếu đã tự chạy Docker infra trước đó:

```powershell
.\start-all.ps1 -SkipInfra
```

## Run One Service With run-service

Khuyến nghị dùng `run-service.cmd` nếu máy bị chặn PowerShell execution policy:

```powershell
cd backend
.\run-service.cmd auth-service -Clean
```

Hoặc chạy trực tiếp file PowerShell:

```powershell
cd backend
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-service.ps1 -Service auth-service -Clean
```

Chạy từng service:

```powershell
.\run-service.cmd auth-service -Clean
.\run-service.cmd user-service -Clean
.\run-service.cmd doctor-service -Clean
.\run-service.cmd appointment-service -Clean
.\run-service.cmd notification-service -Clean
.\run-service.cmd ai-service -Clean
```

`run-service.ps1` tự set default local:

- `APP_PORT` theo service
- `DB_URL` theo database của service
- `DB_USERNAME=healthcare`
- `DB_PASSWORD=healthcare`
- `JWT_SECRET=local-dev-healthcare-jwt-secret-change-before-prod`
- `RABBITMQ_URL=amqp://healthcare:healthcare@localhost:5673`
- `REDIS_HOST=localhost`
- `REDIS_PORT=6379`
- `AI_API_KEY` cho `ai-service` nếu biến môi trường này chưa được set

Override port hoặc database khi cần:

```powershell
.\run-service.cmd ai-service -AppPort 8097 -DbUrl "jdbc:postgresql://localhost:5432/ai_db"
```

## Service Base URLs

| Service | Base URL |
| --- | --- |
| `auth-service` | `http://localhost:8086` |
| `user-service` | `http://localhost:8082` |
| `doctor-service` | `http://localhost:8083` |
| `appointment-service` | `http://localhost:8084` |
| `notification-service` | `http://localhost:8085` |
| `ai-service` | `http://localhost:8087` |

## Auth Seed Data

`auth-service` có Flyway seed tại:

```text
backend/services/auth-service/src/main/resources/db/migration/V3__seed_mvp_users.sql
backend/services/auth-service/src/main/resources/db/migration/V4__seed_admin_user.sql
```

Tài khoản local:

| Role | Email | Password | Actor |
|------|-------|----------|-------|
| Patient | `patient01@healthcare.local` | `Patient@123` | `PATIENT` |
| Doctor  | `doctor01@healthcare.local`  | `Doctor@123`  | `DOCTOR` |
| Admin   | `admin01@healthcare.local`   | `Admin@123`   | `ADMIN` |

## Task 5 — Admin Management

Admin Management được tích hợp vào các service hiện có (không có admin-service riêng):

### Admin Endpoints

| Service | Method | Path | Mô tả |
|---------|--------|------|-------|
| auth-service `:8086` | GET  | `/api/v1/admin/users` | Danh sách user (filter role/status, paginated) |
| auth-service `:8086` | PUT  | `/api/v1/admin/users/{id}/disable` | Vô hiệu hoá user |
| auth-service `:8086` | PUT  | `/api/v1/admin/users/{id}/enable` | Kích hoạt lại user |
| doctor-service `:8083` | GET | `/api/v1/admin/doctors` | Danh sách bác sĩ |
| appointment-service `:8084` | GET | `/api/v1/admin/appointments` | Tất cả lịch hẹn (filter status, paginated) |
| appointment-service `:8084` | PUT | `/api/v1/admin/appointments/{id}/cancel` | Force cancel lịch hẹn |

> Tất cả `/api/v1/admin/**` yêu cầu JWT với `role=ADMIN`. Non-admin token nhận HTTP 403.

### Admin Login

```json
POST /api/v1/auth/login
{
  "email": "admin01@healthcare.local",
  "password": "Admin@123",
  "actor": "ADMIN"
}
```

### Admin UI

Sau khi login với role ADMIN, truy cập admin console tại:

- **Dashboard**: `http://localhost:5173/admin` — Analytics KPI, biểu đồ trạng thái lịch hẹn, health panel
- **Users**: `http://localhost:5173/admin/users` — Quản lý người dùng
- **Doctors**: `http://localhost:5173/admin/doctors` — Danh sách bác sĩ
- **Appointments**: `http://localhost:5173/admin/appointments` — Quản lý lịch hẹn
- **Leaves**: `http://localhost:5173/admin/leaves` — Duyệt nghỉ phép

Admin UI dùng **light/white theme** (AdminShell) tách biệt với dark theme của Patient/Doctor portal.

### Audit Log

Admin actions (disable user, force cancel appointment) được ghi vào bảng `admin_audit_log`:
- `auth_db.admin_audit_log` — track user disable/enable
- `appointment_db.admin_audit_log` — track force cancel

## Postman

Collection và environment local:

- `postman/collections/00-auth.postman_collection.json`
- `postman/collections/01-user.postman_collection.json`
- `postman/collections/02-doctor.postman_collection.json`
- `postman/collections/03-appointment.postman_collection.json`
- `postman/collections/04-notification.postman_collection.json`
- `postman/collections/05-admin.postman_collection.json` ← **Task 5 Admin API**
- `postman/environments/healthcare-local.postman_environment.json`

Postman environment nên có:

- `baseUrlAuth=http://localhost:8086`
- `baseUrlUser=http://localhost:8082`
- `baseUrlDoctor=http://localhost:8083`
- `baseUrlAppointment=http://localhost:8084`
- `baseUrlNotification=http://localhost:8085`
- `baseUrlAI=http://localhost:8087`
- `patientActor=PATIENT`
- `doctorActor=DOCTOR`
- `adminActor=ADMIN`

## Smoke Check

Sau khi chạy services:

```powershell
Invoke-WebRequest http://localhost:8086/actuator/health
Invoke-WebRequest http://localhost:8082/actuator/health
Invoke-WebRequest http://localhost:8083/actuator/health
Invoke-WebRequest http://localhost:8084/actuator/health
Invoke-WebRequest http://localhost:8085/actuator/health
Invoke-WebRequest http://localhost:8087/actuator/health

Invoke-WebRequest http://localhost:8086/api/v1/foundation/ping
Invoke-WebRequest http://localhost:8082/api/v1/foundation/ping
Invoke-WebRequest http://localhost:8083/api/v1/foundation/ping
Invoke-WebRequest http://localhost:8084/api/v1/foundation/ping
Invoke-WebRequest http://localhost:8085/api/v1/foundation/ping
Invoke-WebRequest http://localhost:8087/api/v1/foundation/ping
```

Lưu ý: `ai-service` health có thêm Gemini health indicator. Nếu `AI_API_KEY` không đúng hoặc mạng không gọi được Gemini, `/actuator/health` có thể báo `DOWN` dù service đã start.

## Frontend

```powershell
cd frontend
cmd /c npm.cmd install
cmd /c npm.cmd run dev
```

Frontend Vite mặc định chạy tại:

- `http://localhost:5173`

## Common Startup Issues

- `Connection refused` tới PostgreSQL, Redis hoặc RabbitMQ: kiểm tra `docker compose ps` và đợi container healthy.
- `Port ... is already in use`: service khác đang dùng port đó; dừng process hoặc truyền `-AppPort`.
- `mvn is not recognized`: cài Maven vào `PATH`, dùng IntelliJ bundled Maven, hoặc truyền `-MavenCommand`.
- Lỗi thiếu shared module: chạy từ `backend/` bằng `run-service.ps1` hoặc `run-service.cmd`.
- Lỗi Flyway/database: kiểm tra `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` đúng database của service.
- Nếu PostgreSQL volume cũ thiếu role/database, chạy từ repo root:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\docker\postgres\ensure-healthcare-databases.ps1
```

- Postman trả HTML `Apache/2.4` hoặc `404 Not Found` trên port `8081`: đang gọi nhầm service/port local. Với repo này, `auth-service` chạy ở `http://localhost:8086`.
