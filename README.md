# Healthcare Platform MVP

Phase 0 of the project now includes:

- `backend/` as a Maven multi-module workspace for shared contracts and 5 MVP services.
- `docker/compose.yml` for PostgreSQL, Redis and RabbitMQ.
- `frontend/` as a React + Vite shell with the target route structure for booking flow.

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
```

## Local Infrastructure

```powershell
cd docker
Copy-Item .env.example .env
docker compose up -d
```

PostgreSQL is initialized with 5 databases:

- `auth_db`
- `user_db`
- `doctor_db`
- `appointment_db`
- `notification_db`

## Run Backend Services

Example for `auth-service`:

```powershell
cd backend
$env:APP_PORT='8081'
$env:DB_URL='jdbc:postgresql://localhost:5432/auth_db'
$env:DB_USERNAME='healthcare'
$env:DB_PASSWORD='healthcare'
$env:JWT_SECRET='change-this-before-shared-environments'
$env:RABBITMQ_URL='amqp://healthcare:healthcare@localhost:5672'
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.1\plugins\maven\lib\maven3\bin\mvn.cmd' -pl services/auth-service spring-boot:run
```

Each service exposes:

- actuator health at `/actuator/health`
- foundation ping at `/api/v1/foundation/ping`

## Run Frontend

```powershell
cd frontend
cmd /c npm.cmd install
cmd /c npm.cmd run dev
```
