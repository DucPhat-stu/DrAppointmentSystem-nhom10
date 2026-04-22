# Healthcare Platform MVP

Repository này hiện ở trạng thái MVP nền tảng cho hệ thống healthcare.

- `backend/` là Maven multi-module gồm shared modules và 5 service.
- `docker/compose.yml` dựng hạ tầng local gồm PostgreSQL, Redis và RabbitMQ.
- `frontend/` là React + Vite shell cho luồng auth/booking.
- `auth-service` đã có API nghiệp vụ `register`, `login`, `refresh`, `logout`.
- `user-service`, `doctor-service`, `appointment-service`, `notification-service` hiện mới ở mức foundation endpoint và hạ tầng khởi động.

## Repo Structure

```text
backend/
  pom.xml
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
docker/
  compose.yml
  .env.example
frontend/
  package.json
  vite.config.js
postman/
  collections/
  environments/
docs/
  *.md
```

## Prerequisites

Để chạy local ổn định, máy cần có:

- JDK `21`
- Maven `3.9+` hoặc IntelliJ có bundled Maven
- Docker Desktop
- Node.js `18+`

Lưu ý:

- Chạy backend từ thư mục `backend/` root bằng `-pl ... -am`. Không nên đứng trong từng service module để chạy riêng nếu shared modules chưa được build/install.
- Nếu lệnh `mvn` không có trong `PATH`, thay `mvn` trong README bằng đường dẫn `mvn.cmd` trên máy bạn.
- Đợi `postgres`, `redis`, `rabbitmq` lên trạng thái healthy trước khi chạy service để tránh lỗi kết nối lúc Spring Boot/Flyway khởi động.

## Start Local Infrastructure

```powershell
cd docker
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose up -d
docker compose ps
```

PostgreSQL được khởi tạo sẵn 5 database:

- `auth_db`
- `user_db`
- `doctor_db`
- `appointment_db`
- `notification_db`

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

- `http://localhost:15673`
- username: `healthcare`
- password: `healthcare`

## Service Matrix

| Service | Default port | Database | Extra infra | Current scope |
| --- | --- | --- | --- | --- |
| `auth-service` | `8086` | `auth_db` | Postgres | Auth API + seed data |
| `user-service` | `8082` | `user_db` | Postgres | Foundation only |
| `doctor-service` | `8083` | `doctor_db` | Postgres | Foundation only |
| `appointment-service` | `8084` | `appointment_db` | Postgres + Redis + RabbitMQ | Foundation only |
| `notification-service` | `8085` | `notification_db` | Postgres + RabbitMQ | Foundation only |

Tất cả service đều expose:

- `GET /actuator/health`
- `GET /api/v1/foundation/ping`

Riêng `auth-service` hiện có thêm:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

## Run Backend Services

Use `.\run-service.cmd <service-name> -Clean` from the `backend/` directory. The wrapper bypasses PowerShell execution-policy friction, resolves `JAVA_HOME` for JDK 21, and locates Maven automatically before calling `spring-boot:run`.

If local PostgreSQL was initialized earlier with another role set or another project volume, run `powershell -NoProfile -ExecutionPolicy Bypass -File .\docker\postgres\ensure-healthcare-databases.ps1` once from the repo root to create the expected `healthcare` role and service databases.
Mẫu biến môi trường chung:

```powershell

$env:DB_USERNAME='healthcare'
$env:DB_PASSWORD='healthcare'
$env:JWT_SECRET='change-this-before-shared-environments'
$env:RABBITMQ_URL='amqp://healthcare:healthcare@localhost:5673'
$env:REDIS_HOST='localhost'
$env:REDIS_PORT='6379'
```

Chạy từ `backend/` root:

```powershell
cd backend
```

### Auth Service

```powershell
.\run-service.cmd auth-service -Clean
```

### User Service

```powershell
.\run-service.cmd user-service -Clean
```

### Doctor Service

```powershell
.\run-service.cmd doctor-service -Clean
```

### Appointment Service

```powershell
.\run-service.cmd appointment-service -Clean
```

### Notification Service

```powershell
.\run-service.cmd notification-service -Clean
```

## Auth Seed Data

`auth-service` có Flyway seed tại `backend/services/auth-service/src/main/resources/db/migration/V3__seed_mvp_users.sql`.

Tài khoản seed local:

- Patient: `patient01@healthcare.local / Patient@123`
- Doctor: `doctor01@healthcare.local / Doctor@123`

Seed này được dùng cho login smoke test và Postman collection.

## Postman

Có sẵn collection và environment để test local auth flow:

- `postman/collections/00-auth.postman_collection.json`
- `postman/environments/healthcare-local.postman_environment.json`

Flow hiện có trong collection:

- register patient
- login patient actor
- refresh patient token
- logout patient
- login doctor actor
- refresh doctor token
- logout doctor

### Service Base URLs

| Service | Base URL | Ghi chú |
| --- | --- | --- |
| `auth-service` | `http://localhost:8086` | Có API nghiệp vụ auth |
| `user-service` | `http://localhost:8082` | Mới có foundation endpoint |
| `doctor-service` | `http://localhost:8083` | Mới có foundation endpoint |
| `appointment-service` | `http://localhost:8084` | Mới có foundation endpoint |
| `notification-service` | `http://localhost:8085` | Mới có foundation endpoint |

### Postman URLs

| Service | Method | URL | Ghi chú |
| --- | --- | --- | --- |
| `auth-service` | `POST` | `http://localhost:8086/api/v1/auth/register` | Đăng ký patient |
| `auth-service` | `POST` | `http://localhost:8086/api/v1/auth/login` | Đăng nhập theo actor `PATIENT` hoặc `DOCTOR` |
| `auth-service` | `POST` | `http://localhost:8086/api/v1/auth/refresh` | Lấy access token mới |
| `auth-service` | `POST` | `http://localhost:8086/api/v1/auth/logout` | Thu hồi refresh token |
| `auth-service` | `GET` | `http://localhost:8086/api/v1/foundation/ping` | Foundation smoke check |
| `auth-service` | `GET` | `http://localhost:8086/actuator/health` | Health check |
| `user-service` | `GET` | `http://localhost:8082/api/v1/foundation/ping` | Foundation smoke check |
| `user-service` | `GET` | `http://localhost:8082/actuator/health` | Health check |
| `doctor-service` | `GET` | `http://localhost:8083/api/v1/foundation/ping` | Foundation smoke check |
| `doctor-service` | `GET` | `http://localhost:8083/actuator/health` | Health check |
| `appointment-service` | `GET` | `http://localhost:8084/api/v1/foundation/ping` | Foundation smoke check |
| `appointment-service` | `GET` | `http://localhost:8084/actuator/health` | Health check |
| `notification-service` | `GET` | `http://localhost:8085/api/v1/foundation/ping` | Foundation smoke check |
| `notification-service` | `GET` | `http://localhost:8085/actuator/health` | Health check |

### Postman Environment Suggestion

Nếu bạn muốn tạo environment chung cho tất cả service trong Postman, có thể dùng các biến sau:

- `baseUrlAuth=http://localhost:8086`
- `baseUrlUser=http://localhost:8082`
- `baseUrlDoctor=http://localhost:8083`
- `baseUrlAppointment=http://localhost:8084`
- `baseUrlNotification=http://localhost:8085`
- `patientActor=PATIENT`
- `doctorActor=DOCTOR`

Lưu ý cho Postman:

- `baseUrlAuth` chỉ nên là host gốc, không kèm `/api/v1`.
- Collection local đã tự thêm `/api/v1/auth/...` trong từng request.
- Body login hỗ trợ thêm trường tùy chọn `"actor"` với giá trị `PATIENT` hoặc `DOCTOR`.
- Nếu bạn thấy response HTML `Apache/2.4...` hoặc `404 Not Found` trên port `8081`, bạn đang gọi nhầm service khác trên máy chứ không phải `auth-service`.

## Frontend

```powershell
cd frontend
cmd /c npm.cmd install
cmd /c npm.cmd run dev
```

Frontend Vite chạy mặc định tại:

- `http://localhost:5173`

## Smoke Check

Sau khi chạy service, nên kiểm tra nhanh:

```powershell
Invoke-WebRequest http://localhost:8086/actuator/health
Invoke-WebRequest http://localhost:8086/api/v1/foundation/ping
Invoke-WebRequest http://localhost:8082/api/v1/foundation/ping
Invoke-WebRequest http://localhost:8083/api/v1/foundation/ping
Invoke-WebRequest http://localhost:8084/api/v1/foundation/ping
Invoke-WebRequest http://localhost:8085/api/v1/foundation/ping
```

Nếu chỉ test luồng nghiệp vụ hiện tại, chỉ cần dựng infra rồi chạy `auth-service`.

## Common Startup Issues

- `Connection refused` tới PostgreSQL, Redis hoặc RabbitMQ: kiểm tra lại `docker compose ps` và đợi container healthy.
- `mvn is not recognized`: cài Maven vào `PATH` hoặc dùng Maven bundled từ IntelliJ.
- Lỗi thiếu shared module khi chạy service: hãy chạy từ `backend/` root với `-pl <service> -am`.
- Lỗi Flyway trên `auth-service`: kiểm tra `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` có trỏ đúng `auth_db` hay không.
- Postman trả về HTML `404 Not Found` từ `Apache/2.4`: thường là bạn đang gọi sai port local. Với repo này, `auth-service` nên chạy ở `http://localhost:8086`.




