# Technical Notes

## Architecture Summary

The platform is split into six Spring Boot services and one React/Vite frontend:

| Service | Responsibility |
| --- | --- |
| auth-service | Credentials, JWT, role-based access, mock OTP/2FA/demo auth flows |
| user-service | User profile, avatar upload, medical records, doctor certifications |
| doctor-service | Doctor directory, schedules, leaves, clinical appointment context |
| appointment-service | Booking lifecycle, appointment state transitions, reminder scheduling |
| notification-service | Notification inbox, preferences, appointment event consumption, broadcast |
| ai-service | Symptom assistant, conversation history, feedback, mock analytics |

Appointment events are published through an outbox/RabbitMQ flow and consumed by notification-service.

## Demo Mode Decisions

- OTP and 2FA use the fixed code `123456`.
- Doctor code login uses seeded code `DOCTOR-0001`.
- Password reset tokens are logged to the auth-service console.
- AI image analysis and analytics are mock implementations.
- The API shape is intentionally close to a real integration so providers can be replaced later.

## Trade-offs

| Decision | Reason | Production Direction |
| --- | --- | --- |
| Local avatar storage | Fast and reliable for local demo | Move to S3/MinIO/CDN with signed URLs |
| Mock OTP/2FA | Avoid SMS/email provider setup for demo | Use SMS/email/TOTP provider with rate limiting |
| Mock AI analytics | Demonstrate UX and API boundaries | Replace with validated models and clinical review workflow |
| Cross-service UUID references | Keeps services independent | Add consistency checks, monitoring, and reconciliation jobs |
| Limited automated testing | Optimized for demo timeline | Add integration and E2E coverage before release |

## Known Limitations

- This is not production medical software.
- AI output is advisory only and does not replace clinical diagnosis.
- Demo notifications depend on local RabbitMQ availability.
- File upload hardening is intentionally minimal and should be expanded before production.
- Observability is limited to logs and health endpoints.

## Recommended Next Steps

1. Add integration tests for appointment state transitions and notification consumption.
2. Move demo mock flows behind explicit Spring profiles.
3. Add audit logs for all security-sensitive user actions.
4. Add object storage for uploaded files.
5. Add Playwright smoke tests for login, booking, notifications, and AI assistant.
