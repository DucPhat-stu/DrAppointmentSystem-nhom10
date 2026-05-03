# Demo Checklist

Use this checklist before presenting. Mark each item as PASS, FAIL, or SKIPPED and add notes for anything unstable.

| Area | Scenario | Expected Result | Status | Notes |
| --- | --- | --- | --- | --- |
| Auth | Patient login with `patient01@healthcare.local` | Patient portal opens |  |  |
| Auth | Doctor login with `doctor01@healthcare.local` | Doctor appointment dashboard is available |  |  |
| Auth | Admin login with `admin01@healthcare.local` | Admin console opens |  |  |
| Auth | Phone OTP login | Code `123456` signs in the seeded phone user |  |  |
| Auth | Doctor code login | Code `DOCTOR-0001` signs in as doctor |  |  |
| Auth | Forgot/reset password | Reset token is logged by auth-service and password can be changed |  |  |
| Patient | View doctor list | Doctors render with profile information |  |  |
| Patient | Book appointment | Appointment is created as pending |  |  |
| Patient | View my appointments | New appointment appears in the list |  |  |
| Doctor | View appointment dashboard | Pending appointment is visible |  |  |
| Doctor | Confirm appointment | Status changes to confirmed |  |  |
| Doctor | Reject appointment | Status changes to rejected and reason is saved |  |  |
| Doctor | Save SOAP note | SOAP note is persisted for the appointment |  |  |
| Profile | Upload avatar | Image file uploads and profile updates |  |  |
| Profile | Add certification | Certification appears in professional profile |  |  |
| Notification | Appointment event notification | Patient receives appointment update |  |  |
| Notification | Mark read | Notification read badge changes |  |  |
| Notification | Update preferences | Preference toggle persists after refresh |  |  |
| Admin | Search users | Query filters users by name/email/phone |  |  |
| Admin | Disable and enable user | Status changes and action succeeds |  |  |
| Admin | Broadcast notification | Selected users receive broadcast |  |  |
| AI | Symptom check | Assistant returns formatted advisory response |  |  |
| AI | Conversation history | Last exchange appears in history |  |  |
| AI | Feedback | Helpful/not helpful feedback is accepted |  |  |
| AI | Doctor recommendation | Matching doctor card is shown |  |  |
| AI | Image analysis | Uploaded image returns mock analysis |  |  |
| AI | Follow-up suggestion | Follow-up window is shown |  |  |
| AI | Wait-time prediction | Estimated wait time is shown |  |  |
| AI | Disease trends | Trend cards render |  |  |
| AI | Risk alert | Risk level and next step are shown |  |  |

## Verification Commands

```powershell
cd main\backend
mvn -pl services/auth-service,services/user-service,services/appointment-service,services/notification-service,services/ai-service -am compile -DskipTests
```

```powershell
cd main\frontend
npm run build
```
