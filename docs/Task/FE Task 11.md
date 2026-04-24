### Sprint 1
- Profile layout
- Personal info UI
### Sprint 2
- Edit + API personal info
- Medical records list
### Sprint 3
- Record detail
- Animation + responsive


### DEFINITION OF DONE
## Functional
- Load được profile
- Edit personal info
- View medical records
## Security
- Không edit medical records
- Không expose data sai
## UI/UX
- Có loading
- Có error handling
- Có animation nhẹ

### EPIC: USER PROFILE MANAGEMENT
## Story 1: Profile Page Layout
# FE: Setup Route Profile Page

- Description:

- Tạo route /profile
- Chỉ cho phép user đã login truy cập

- Subtasks:
- FE: Add route vào router
- FE: Guard route (redirect nếu chưa login)

Acceptance Criteria:

- Không login → redirect /login
- Login → vào được /profile
FE: Build Profile Layout

- Description:

- Layout chia 2 section rõ ràng:
Personal Information
Medical Records

- Subtasks:

- FE: Tạo <ProfileLayout />
- FE: Section wrapper component
- FE: Divider UI

- Acceptance Criteria:

- UI phân tách rõ 2 phần
- Không gây rối thông tin

### PERSONAL INFORMATION (EDITABLE)
## Story 2: Personal Info UI
# FE: Build Personal Information Section

- Fields:

Fullname
Address
Phone Number
Email
FE: Component Breakdown

Subtasks:
- FE: InputField reuse
- FE: Form wrapper
- FE: Label + value display mode
- FE: UI Modes

2 trạng thái:

View mode
Edit mode
FE: Toggle Edit Mode

Subtasks:
- FE: Button "Edit"
- FE: Switch sang input field
- FE: Button "Save" + "Cancel"

Acceptance Criteria:

Click Edit → chuyển sang editable
Click Save → lưu data
Click Cancel → revert data
## Story 3: Personal Info Logic
### FE: Load User Data
Subtasks:

- Call API /user/profile
Bind data vào form
### FE: Update User Info

Subtasks:

- PUT /user/profile
- Handle loading state
FE: Validation

Rules:

Field	Rule
Fullname	required
Phone	number format
Email	đúng format

Acceptance Criteria:

Không save nếu invalid
Show error message rõ ràng
## Story 4: UX Personal Info
### FE: UX Enhancement
Subtasks:

- Disable input khi view mode
Loading spinner khi save
Toast success / error
## FE: Animation

Subtasks:

- Fade transition khi toggle edit
Input focus glow
## Story 5: Medical Records List
### FE: Build Medical Records Section

Description:
Hiển thị danh sách hồ sơ bệnh án

FE: Data Structure
{
  "recordId": "000001",
  "visitDate": "2026-04-01",
  "appointmentDate": "2026-03-28",
  "diseaseSummary": "Viêm họng",
  "department": "Tai Mũi Họng",
  "doctor": "Dr. A",
  "prescription": "...",
  "checkinTime": "2026-04-01T08:30",
  "tests": ["Xét nghiệm máu"]
}
FE: UI List

Subtasks:

FE: Table hoặc Card list
FE: Pagination (optional)

Acceptance Criteria:

Hiển thị danh sách rõ ràng
Không editable
## Story 6: Medical Record Detail View
### FE: Expand / Detail View

Subtasks:

- FE: Click record → expand hoặc modal
FE: Show full info
FE: Display Fields
Mã bệnh án (6 số)
Ngày khám
Ngày đặt lịch
Tên bệnh
Khoa khám
Bác sĩ
Toa thuốc
Check-in time
Xét nghiệm

Acceptance Criteria:

Thông tin đầy đủ
Read-only hoàn toàn
Story 7: Security & Data Protection
### FE: Restrict Editing

Subtasks:

Disable toàn bộ input
Không có button edit
FE: UI Warning

Optional:

Hiển thị label:
"Medical records are read-only"

Acceptance Criteria:

User không thể sửa
Không có API update gọi đi
## Story 8: Medical Records API Integration
### FE: Fetch Medical Records

Subtasks:

- GET /medical-records
Loading skeleton
FE: Error Handling

Subtasks:

Handle API fail
Show empty state
### SHARED COMPONENTS (PROFILE)
## Story 9: Profile Components
### FE: ProfileSection Component
<ProfileSection title="Personal Information">
  {children}
</ProfileSection>
### FE: InfoRow Component
label | value
FE: RecordCard Component
dùng cho medical records
### UX & UI REQUIREMENTS
## Layout
- Card-based UI
- Shadow nhẹ
- Padding 16–24
### Visual Hierarchy
- Personal Info: ưu tiên cao
- Medical Records: collapse/secondary
## Color
- Editable: accent xanh
Read-only: xám nhẹ