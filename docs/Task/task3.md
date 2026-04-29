🟢 TỔNG QUAN CHIẾN LƯỢC SPRINT
Sprint	Mục tiêu chính	Deliverable
Sprint 1	AI chạy end-to-end	Chatbot trả kết quả đúng
Sprint 2	Chuẩn hóa input	Form → AI chính xác hơn
Sprint 3	Dynamic prompt	Admin control prompt
🚀 SPRINT 1 — AI CHATBOT CORE (MVP)
🎯 Sprint Goal

Hệ thống nhận input → gọi AI → trả kết quả text rõ ràng (bệnh + chuyên khoa)

📦 Scope (BẮT BUỘC)
Input: text đơn giản (không cần form)
Output:
possible_conditions
symptoms_detected
recommended_specialty
advice
Chat UI cơ bản
❌ Out of Scope
Admin panel
Dynamic prompt
Mapping doctor DB
Structured form
📘 STORIES
STORY 1: AI Integration (BE)
Tasks
Setup AI API (Gemini free)
Hardcode prompt chuẩn JSON
Call API
Condition
Timeout < 2s
Không gọi AI trực tiếp từ FE
Acceptance Criteria
API trả về response thành công
Có text output (không rỗng)
STORY 2: AI Response Parser (BE)
Tasks
Parse JSON từ AI
Validate fields
Fallback nếu lỗi
Condition
Không được throw exception ra ngoài
Acceptance Criteria
Luôn trả object hợp lệ
Không crash nếu AI trả sai format
STORY 3: Text Formatter (BE)
Tasks
Convert JSON → text
Thêm disclaimer
Condition
Không hiển thị null / []
Acceptance Criteria
Text readable
Có đầy đủ 4 phần thông tin
STORY 4: API Endpoint (BE)
Tasks
POST /ai/check
Input: text
Output: formatted text
Condition
Idempotent API
Acceptance Criteria
Response time < 2s
Format consistent
STORY 5: Chat UI (FE)
Tasks
Input box
Send button
Chat bubble
Condition
Không reload page
Acceptance Criteria
User gửi → thấy response ngay
UI không bị vỡ layout
STORY 6: Loading & Error (FE)
Tasks
Loading state
Error message
Acceptance Criteria
Không spam request
Có thông báo khi lỗi
⚠️ RỦI RO SPRINT 1
Risk	Level	Mitigation
AI trả sai JSON	🔴 Cao	fallback parser
API timeout	🟠 Trung	retry 1 lần
UX khó hiểu	🟢 Thấp	placeholder
🚀 SPRINT 2 — STRUCTURED INPUT
🎯 Sprint Goal

Chuẩn hóa input từ user → giúp AI hiểu tốt hơn

📦 Scope
Form input:
symptoms
duration
description
BE build prompt (hardcode template)
❌ Out of Scope
Admin prompt
Dynamic template
📘 STORIES
STORY 1: Symptom Form UI (FE)
Tasks
Input triệu chứng
Input thời gian
Textarea mô tả
Condition
Validate bắt buộc symptoms
Acceptance Criteria
Không submit nếu thiếu dữ liệu
STORY 2: Input DTO & Validation (BE)
Tasks
Create DTO
Validate length, null
Condition
Sanitize input (XSS basic)
STORY 3: Prompt Builder (STATIC) (BE)
Tasks
Convert JSON → text
Inject vào prompt template
Condition
Template hardcode trong code
Acceptance Criteria
Không còn placeholder {{}} trong output
STORY 4: Integrate AI Flow
Tasks
Replace text input bằng structured input
Call AI
Acceptance Criteria
Output chính xác hơn sprint 1
STORY 5: Preview Prompt (Optional nhưng nên có)
Tasks
Hiển thị text sẽ gửi AI
⚠️ RỦI RO SPRINT 2
Risk	Level	Mitigation
User nhập sai	🟠 Trung	validate
Prompt không tự nhiên	🟠 Trung	refine template
FE build sai format	🔴 Cao	BE build prompt
🚀 SPRINT 3 — ADMIN PROMPT MANAGEMENT
🎯 Sprint Goal

Admin có thể chỉnh prompt mà không cần deploy code

📦 Scope
CRUD prompt template
Dynamic prompt builder
❌ Out of Scope
AI training
ML optimization
📘 STORIES
STORY 1: Prompt Template DB (BE)
Tasks
Create table
Seed data
Condition
Chỉ 1 template active
STORY 2: Prompt CRUD API (BE)
Tasks
Create/update/delete
Validate {{variables}}
Condition
Reject template sai format
STORY 3: Admin UI (FE)
Tasks
List
Edit
Toggle active
STORY 4: Dynamic Prompt Builder (BE)
Tasks
Load template DB
Replace variables
Condition
Không để sót {{}} trong output
STORY 5: Preview Prompt (FE + BE)
Tasks
API preview
UI preview
⚠️ RỦI RO SPRINT 3
Risk	Level	Mitigation
Admin nhập sai template	🔴 Cao	validate
Missing variable	🔴 Cao	default value
Over-flexible prompt	🟠 Trung	limit format
🔒 GLOBAL RULES (ÁP DỤNG TOÀN BỘ SPRINT)
❌ CẤM
FE build prompt
Hardcode AI response
Skip validation
✅ BẮT BUỘC
BE control toàn bộ AI flow
JSON → text conversion luôn tồn tại
Có disclaimer
🎯 KẾT LUẬN
✔ Sprint plan này đảm bảo:
Có MVP usable ngay Sprint 1
Giảm rủi ro fail AI
Không over-engineering
Nếu làm sai thứ tự:
80–90% fail sprint đầu
Dev bị kẹt ở prompt thay vì feature