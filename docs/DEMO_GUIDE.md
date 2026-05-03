# Healthcare Platform Demo Guide

This guide is written for a short, professional demo of the Healthcare Appointment Platform. The current build is demo-grade: it shows the intended product workflows and API boundaries, while selected integrations are mocked.

## Prerequisites

- Docker Desktop
- JDK 21
- Maven 3.9+ or the Maven wrapper/cache used by the local scripts
- Node.js 18+
- Git

## Start The Local Stack

From the repository root:

```powershell
cd main
cd backend
.\start-all.ps1
```

This starts PostgreSQL, Redis, RabbitMQ, ensures the six service databases exist, and launches the Spring Boot services.

Start the frontend in a second terminal:

```powershell
cd main\frontend
npm install
npm run dev
```

Open the UI at:

```text
http://localhost:5173
```

## Service Ports

| Service | URL |
| --- | --- |
| auth-service | `http://localhost:8086` |
| user-service | `http://localhost:8082` |
| doctor-service | `http://localhost:8083` |
| appointment-service | `http://localhost:8084` |
| notification-service | `http://localhost:8085` |
| ai-service | `http://localhost:8087` |
| RabbitMQ management | `http://localhost:15673` |

RabbitMQ credentials:

```text
username: healthcare
password: healthcare
```

## Demo Accounts

| Role | Email | Password | Primary Demo Area |
| --- | --- | --- | --- |
| Patient | `patient01@healthcare.local` | `Patient@123` | Booking, profile, notifications, AI assistant |
| Doctor | `doctor01@healthcare.local` | `Doctor@123` | Appointment dashboard, SOAP notes, reject/confirm flow |
| Admin | `admin01@healthcare.local` | `Admin@123` | User search, appointment management, broadcast notifications |

Mock demo codes:

| Flow | Code |
| --- | --- |
| Phone OTP login | `123456` |
| Two-factor authentication | `123456` |
| Doctor code login | `DOCTOR-0001` |

## Recommended 5-7 Minute Demo Flow

1. Sign in as Patient.
2. Open Doctors and book an available appointment.
3. Sign out and sign in as Doctor.
4. Open Doctor Appointments and confirm or reject the appointment.
5. Sign back in as Patient and open Notifications.
6. Open Profile, upload an avatar, and add a professional certification if using a doctor account.
7. Open AI Assistant and run:
   - Symptom check
   - Doctor recommendation
   - Mock image analysis
   - Wait-time prediction
   - Disease trend dashboard
8. Sign in as Admin and show:
   - User search
   - Appointment management
   - Broadcast notification

## Demo Mode Notes

- OTP and 2FA use fixed mock code `123456`.
- Doctor code login uses seeded code `DOCTOR-0001`.
- Password reset tokens are logged by auth-service instead of being emailed.
- AI image analysis, wait-time prediction, disease trends, and risk alerts are mock demo flows.
- Avatar upload stores files on local disk under `uploads/avatars`. Production should use object storage such as S3 or MinIO.

## Smoke Verification

Backend compile without tests:

```powershell
cd main\backend
mvn -pl services/auth-service,services/user-service,services/appointment-service,services/notification-service,services/ai-service -am compile -DskipTests
```

Frontend production build:

```powershell
cd main\frontend
npm run build
```

Health checks:

```powershell
Invoke-WebRequest http://localhost:8086/actuator/health
Invoke-WebRequest http://localhost:8082/actuator/health
Invoke-WebRequest http://localhost:8083/actuator/health
Invoke-WebRequest http://localhost:8084/actuator/health
Invoke-WebRequest http://localhost:8085/actuator/health
Invoke-WebRequest http://localhost:8087/actuator/health
```

## Common Demo Issues

| Symptom | Fix |
| --- | --- |
| Frontend cannot reach services | Confirm backend services are running and Vite proxy is active. |
| RabbitMQ notification does not appear immediately | Wait a few seconds and refresh Notifications. |
| AI health reports DOWN | The Gemini indicator can fail without a real API key; mock demo endpoints still show the UI flow. |
| Port already in use | Stop the process using the port or pass a custom port to `run-service.ps1`. |
| Maven or Node not found | Add them to PATH or use the full paths documented in the local README. |
