# TASK 3: AI CHATBOT IMPLEMENTATION PLAN
**Created:** 2026-04-29  
**Version:** 1.0  
**Status:** IMPLEMENTED - VALIDATION PENDING  

---

## 📋 EXECUTIVE SUMMARY

**Mục tiêu:** Triển khai hệ thống AI Chatbot cho Healthcare Platform qua 3 Sprints
- **Sprint 1:** AI core MVP (basic chatbot)
- **Sprint 2:** Chuẩn hóa input form
- **Sprint 3:** Admin prompt management

**Quy trình:**
1. Đọc tài liệu → Lập kế hoạch ✅ (HOÀN TẤT)
2. **Phân tích hiện trạng BE/FE** → So sánh với task3
3. **Lên bảng kế hoạch chi tiết** (THIS DOCUMENT)
4. Thực hiện từng Story → Commit git sau mỗi story
5. Sau Task3 hoàn tất: Phân tích lỗi, sửa, commit
6. Phân tích rủi ro vận hành & deployment

---

## 📊 PHÂN TÍCH HIỆN TRẠNG

### Backend
| Service | Status | Notes |
|---------|--------|-------|
| auth-service | ✅ Implemented | JWT, refresh token, RBAC |
| user-service | ✅ Implemented | User profile, doctor directory |
| doctor-service | ✅ Implemented | TimeSlot management |
| appointment-service | ✅ Implemented | Booking, cancel, reschedule |
| notification-service | ✅ Implemented | Event consumer |
| **ai-service** | ✅ IMPLEMENTED | Spring Boot module on port 8087, database `ai_db` |

### Frontend
| Component | Status | Notes |
|-----------|--------|-------|
| Auth (Login/Register) | ✅ Done | Session management |
| Doctor List & Detail | ✅ Done | Browse doctors |
| Appointment Booking | ✅ Done | Book, view, cancel |
| Notifications | ✅ Done | View notifications |
| Profile | ✅ Done | User profile |
| **AI Chatbot Page** | ✅ IMPLEMENTED | `frontend/src/pages/ChatbotPage.jsx`, route `/chat` |

### Current Implementation Status
- ✅ **Phase 0-3 Complete:** Auth, User, Doctor, Appointment, Notification
- ✅ **Task3 Implemented:** AI service, chat UI, structured input, prompt management
- **Current Codebase:** Stable, ready to extend

---

## 🎯 TASK 3 SPRINT BREAKDOWN

### SPRINT 1: AI CHATBOT CORE (MVP) — 5-7 Working Days

#### 🎯 Sprint Goal
Hệ thống nhận input text → gọi AI (Gemini) → trả kết quả text rõ ràng

#### 📦 Scope
- Input: text đơn giản (text box)
- Output: possible_conditions, symptoms_detected, recommended_specialty, advice
- Chat UI cơ bản (input box, send button, chat bubble)
- ❌ Out: Admin panel, Dynamic prompt, Mapping doctor DB

#### 🚀 STORIES & TASKS

---

### **SPRINT 1 - STORY 1: Backend Setup & AI Integration**
**Duration:** 2 days | **Owner:** Backend Dev  
**Branch:** `feature/ai-service-setup`

**Tasks:**

1. **Create ai-service Maven module**
   - Thư mục: `backend/services/ai-service`
   - Copy từ `appointment-service` structure
   - pom.xml, application.yml, db migration

2. **Setup API credentials (Gemini Free)**
   ```
   - Tạo Google Cloud Project
   - Enable Gemini API
   - Get API Key (free tier: 15 req/min)
   - Lưu vào environment variable: AI_API_KEY
   ```

3. **Create AIClient (HTTP wrapper)**
   ```java
   // backend/services/ai-service/src/main/java/com/healthcare/ai/infrastructure/AIClient.java
   
   @Component
   public class AIClient {
       @Value("${ai.gemini.api-key}")
       private String apiKey;
       
       @Value("${ai.gemini.model:gemini-pro}")
       private String model;
       
       @Value("${ai.timeout.ms:2000}")
       private long timeoutMs;
       
       public GeminiResponse callAI(String prompt) throws AIException {
           // HTTP POST to https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
           // Timeout: 2s
           // Retry: 1 time if timeout
           // Return raw response
       }
   }
   ```

4. **Setup hardcoded prompt template**
   ```
   // Prompt v1 (hardcoded in code)
   Template:
   "Analyze the following health symptoms and provide JSON output:
   Symptoms: {symptoms}
   
   Return ONLY valid JSON:
   {
     "possible_conditions": ["condition1", "condition2"],
     "symptoms_detected": ["symptom1", "symptom2"],
     "recommended_specialty": "specialty_name",
     "advice": "brief advice"
   }"
   ```

**Acceptance Criteria:**
- ✅ AI Service starts on port 8085
- ✅ Health endpoint `/ai/health` returns 200
- ✅ AIClient can call Gemini API
- ✅ Response received within 2s
- ✅ No exception thrown (fallback implemented)

**Commit Message:**
```
feat(ai-service): Setup ai-service module and Gemini API client

- Create ai-service Maven module with Spring Boot structure
- Implement AIClient with timeout and retry logic
- Setup Gemini API integration (free tier)
- Add health endpoint
- Configure environment variables
```

---

### **SPRINT 1 - STORY 2: Response Parser**
**Duration:** 1.5 days | **Owner:** Backend Dev  
**Branch:** `feature/ai-response-parser`

**Tasks:**

1. **Create Response DTO**
   ```java
   // backend/services/ai-service/src/main/java/com/healthcare/ai/dto/AICheckResponse.java
   
   public record AICheckResponse(
       List<String> possibleConditions,
       List<String> symptomsDetected,
       String recommendedSpecialty,
       String advice
   ) {}
   ```

2. **Create Parser component**
   ```java
   // backend/services/ai-service/src/main/java/com/healthcare/ai/application/AIResponseParser.java
   
   @Component
   public class AIResponseParser {
       public AICheckResponse parse(String rawJsonResponse) {
           try {
               // Parse JSON
               // Validate fields (not null, not empty array)
               // Return structured response
           } catch (JsonException e) {
               // Log error
               // Return fallback response
               return new AICheckResponse(
                   List.of("Không xác định"),
                   List.of("Không thể phân tích"),
                   "Nội tổng quát",
                   "Vui lòng thử lại hoặc liên hệ bác sĩ"
               );
           }
       }
   }
   ```

3. **Add validation rules**
   - possibleConditions: not empty, max 5 items
   - symptomsDetected: not empty, max 10 items
   - recommendedSpecialty: required, non-empty
   - advice: required, min 10 chars

**Acceptance Criteria:**
- ✅ Valid JSON parsed correctly
- ✅ Invalid JSON returns fallback (NO exception)
- ✅ All fields validated
- ✅ Unit tests cover happy path & error cases

**Commit Message:**
```
feat(ai-service): Implement response parser with fallback

- Create AICheckResponse DTO
- Implement AIResponseParser with JSON validation
- Add fallback for malformed responses
- Unit tests for happy path and error cases
```

---

### **SPRINT 1 - STORY 3: Text Formatter**
**Duration:** 1 day | **Owner:** Backend Dev  
**Branch:** `feature/ai-text-formatter`

**Tasks:**

1. **Create Formatter component**
   ```java
   // backend/services/ai-service/src/main/java/com/healthcare/ai/application/AITextFormatter.java
   
   @Component
   public class AITextFormatter {
       public String format(AICheckResponse response) {
           return String.format(
               "🏥 Các bệnh có thể: %s\n\n" +
               "📋 Triệu chứng ghi nhận: %s\n\n" +
               "👨‍⚕️ Gợi ý khám: %s\n\n" +
               "💡 Lời khuyên: %s\n\n" +
               "⚠️ Lưu ý: Đây chỉ là gợi ý từ AI, không thay thế chẩn đoán y khoa",
               String.join(", ", response.possibleConditions()),
               String.join(", ", response.symptomsDetected()),
               response.recommendedSpecialty(),
               response.advice()
           );
       }
   }
   ```

2. **Format rules**
   - Không hiển thị null/empty arrays
   - Thêm emoji cho clarity
   - Thêm disclaimer cuối
   - Vietnamese text, dễ đọc

**Acceptance Criteria:**
- ✅ Output readable
- ✅ Có đủ 4 phần thông tin
- ✅ Không hiển thị null
- ✅ Có disclaimer

**Commit Message:**
```
feat(ai-service): Implement text formatter with disclaimer

- Create AITextFormatter for user-friendly output
- Format: conditions, symptoms, specialty, advice
- Add disclaimer to avoid medical liability
- Unit tests for formatting logic
```

---

### **SPRINT 1 - STORY 4: API Endpoint**
**Duration:** 1 day | **Owner:** Backend Dev  
**Branch:** `feature/ai-check-endpoint`

**Tasks:**

1. **Create Controller**
   ```java
   // backend/services/ai-service/src/main/java/com/healthcare/ai/controller/AIController.java
   
   @RestController
   @RequestMapping("/api/v1/ai")
   public class AIController {
       
       @PostMapping("/check")
       public ResponseEntity<ApiResponse<String>> checkSymptoms(
           @Valid @RequestBody AICheckRequest request,
           @RequestHeader("X-User-Id") String userId
       ) {
           // Input: {text: "Ho, sot, mat ngon an"}
           // Process: 
           //   - Call AIClient
           //   - Parse response
           //   - Format text
           // Output: formatted text string
           // Timeout: < 2s
           // Idempotent: không idempotent (mỗi call là mới)
       }
   }
   ```

2. **Create Request DTO**
   ```java
   public record AICheckRequest(
       @NotBlank(message = "Symptoms text is required")
       @Length(min = 5, max = 500)
       String text
   ) {}
   ```

3. **API Contract**
   ```
   POST /api/v1/ai/check
   
   Request:
   {
     "text": "Ho khan, sot cao, dau dau"
   }
   
   Response (Success 200):
   {
     "success": true,
     "data": "🏥 Các bệnh có thể: Cúm, Viêm họng...",
     "meta": { "requestId": "...", "timestamp": "..." }
   }
   
   Response (Error 400):
   {
     "success": false,
     "errorCode": "VALIDATION_ERROR",
     "message": "Symptoms text is required",
     "meta": { "requestId": "...", "timestamp": "..." }
   }
   ```

**Acceptance Criteria:**
- ✅ Response time < 2s
- ✅ Format consistent
- ✅ Validation on input
- ✅ Idempotent API (can call multiple times)

**Commit Message:**
```
feat(ai-service): Add /api/v1/ai/check endpoint

- Create AIController with POST /check
- Validate input (text: 5-500 chars)
- Integrate AIClient → Parser → Formatter
- Response time < 2s
- Add integration tests with mock AI
```

---

### **SPRINT 1 - STORY 5: Chat UI (Frontend)**
**Duration:** 2 days | **Owner:** Frontend Dev  
**Branch:** `feature/chat-ui-basic`

**Tasks:**

1. **Create ChatbotPage component**
   ```jsx
   // frontend/src/pages/ChatbotPage.jsx
   
   export default function ChatbotPage() {
     const [messages, setMessages] = useState([]);
     const [inputText, setInputText] = useState('');
     const [loading, setLoading] = useState(false);
     const [error, setError] = useState(null);
     
     const handleSend = async () => {
       // 1. Validate input (not empty, not too long)
       // 2. Add user message to UI
       // 3. Call AI API
       // 4. Add AI response as bubble
       // 5. Clear input
     };
     
     return (
       <div className={styles.container}>
         <div className={styles.chatBox}>
           {messages.map(msg => (
             <div key={msg.id} className={msg.role === 'user' ? styles.userBubble : styles.aiBubble}>
               {msg.text}
             </div>
           ))}
         </div>
         <div className={styles.inputBox}>
           <input 
             value={inputText}
             onChange={(e) => setInputText(e.target.value)}
             placeholder="Mô tả triệu chứng của bạn..."
             disabled={loading}
           />
           <button onClick={handleSend} disabled={loading}>
             {loading ? 'Đang xử lý...' : 'Gửi'}
           </button>
         </div>
       </div>
     );
   }
   ```

2. **Create Chat Service**
   ```javascript
   // frontend/src/services/chatService.js
   
   export const chatService = {
     async checkSymptoms(text) {
       return httpClient.post('/ai/check', { text });
     }
   };
   ```

3. **CSS Module**
   - Chat bubbles (user right, AI left)
   - Input box + send button
   - Loading state
   - Mobile responsive

**Acceptance Criteria:**
- ✅ Không reload page
- ✅ User gửi → thấy response ngay
- ✅ UI không bị vỡ layout
- ✅ Loading indicator visible

**Commit Message:**
```
feat(frontend): Add ChatbotPage with basic chat UI

- Create ChatbotPage with input box and chat bubbles
- Integrate with AI check endpoint
- Add loading and error states
- Mobile responsive design
- CSS Modules for styling
```

---

### **SPRINT 1 - STORY 6: Loading & Error Handling**
**Duration:** 1 day | **Owner:** Frontend Dev  
**Branch:** `feature/chat-error-handling`

**Tasks:**

1. **Add Loading State**
   - Show spinner/skeleton while waiting
   - Disable input/button during request
   - Timeout message if > 2s

2. **Add Error Handling**
   ```jsx
   {error && (
     <div className={styles.errorBox}>
       ⚠️ {error.message || 'Có lỗi xảy ra, vui lòng thử lại'}
     </div>
   )}
   ```

3. **Network error handling**
   - Timeout error: "Yêu cầu quá lâu, vui lòng thử lại"
   - 400 error: Show validation message
   - 500 error: "Lỗi server, vui lòng liên hệ hỗ trợ"
   - Network error: "Kiểm tra kết nối internet"

4. **Prevent spam**
   - Cooldown: 2s between requests
   - Max retry: 1 time per request

**Acceptance Criteria:**
- ✅ Không spam request
- ✅ Có thông báo khi lỗi
- ✅ User-friendly error messages
- ✅ Graceful fallback

**Commit Message:**
```
feat(frontend): Add error handling and loading states to chat

- Add loading spinner and disabled input during request
- Handle network, validation, and server errors
- Implement 2s cooldown to prevent spam
- User-friendly error messages in Vietnamese
```

---

## 🎯 SPRINT 2: STRUCTURED INPUT — 5-7 Working Days

### Sprint Goal
Chuẩn hóa input từ form → giúp AI hiểu tốt hơn

### Stories

### **SPRINT 2 - STORY 1: Symptom Form UI**
**Branch:** `feature/symptom-form-ui`

**Tasks:**
1. Create form with fields:
   - Triệu chứng (required, multi-select)
   - Thời gian (required, select: < 1 ngày, 1-3 ngày, > 3 ngày)
   - Mô tả (optional textarea)

2. Validate before submit
3. CSS Module styling

### **SPRINT 2 - STORY 2: Input DTO & Validation (BE)**
**Branch:** `feature/structured-input-dto`

**Tasks:**
1. Create StructuredAICheckRequest DTO
2. Sanitize input (prevent XSS)
3. Validate length, null checks
4. Unit tests

### **SPRINT 2 - STORY 3: Prompt Builder (Static)**
**Branch:** `feature/static-prompt-builder`

**Tasks:**
1. Create PromptBuilder component
2. Build natural language prompt from structured input
3. Inject into hardcoded template
4. No {{}} placeholders in output

### **SPRINT 2 - STORY 4: Integrate Structured Flow**
**Branch:** `feature/structured-ai-flow`

**Tasks:**
1. Replace text input with form
2. Call AI with structured prompt
3. Parse & format as usual
4. Integration tests

### **SPRINT 2 - STORY 5: Preview Prompt (Optional)**
**Branch:** `feature/prompt-preview`

**Tasks:**
1. Show prompt text before sending
2. Let user review and edit

---

## 🎯 SPRINT 3: ADMIN PROMPT MANAGEMENT — 7-10 Working Days

### Sprint Goal
Admin có thể chỉnh prompt mà không cần deploy code

### Stories

### **SPRINT 3 - STORY 1: Prompt Template DB**
**Branch:** `feature/prompt-template-db`

**Tasks:**
1. Create prompt_templates table:
   ```sql
   CREATE TABLE prompt_templates (
     id UUID PRIMARY KEY,
     name VARCHAR(255),
     template TEXT (contains {{variables}}),
     variables_list TEXT (JSON array of var names),
     is_active BOOLEAN,
     version INT,
     created_at TIMESTAMP,
     updated_at TIMESTAMP
   )
   ```
2. Seed default template
3. Constraint: only 1 template active

### **SPRINT 3 - STORY 2: Prompt CRUD API**
**Branch:** `feature/prompt-crud-api`

**Tasks:**
1. POST /api/v1/ai/templates (create)
2. GET /api/v1/ai/templates (list)
3. GET /api/v1/ai/templates/{id}
4. PUT /api/v1/ai/templates/{id} (update)
5. DELETE /api/v1/ai/templates/{id}
6. PATCH /api/v1/ai/templates/{id}/activate

### **SPRINT 3 - STORY 3: Admin UI (FE)**
**Branch:** `feature/admin-prompt-ui`

**Tasks:**
1. Admin page to list templates
2. Edit template form
3. Toggle active template
4. Preview before save

### **SPRINT 3 - STORY 4: Dynamic Prompt Builder**
**Branch:** `feature/dynamic-prompt-builder`

**Tasks:**
1. Load template from DB
2. Replace {{variables}} with values
3. Validate no sót {{}} in output

### **SPRINT 3 - STORY 5: Preview Endpoint**
**Branch:** `feature/prompt-preview-api`

**Tasks:**
1. POST /api/v1/ai/templates/preview
2. Build prompt without calling AI
3. Return formatted prompt

---

## 🔄 GIT WORKFLOW FOR EACH STORY

### Step 1: Create Feature Branch
```bash
git checkout main
git pull origin main
git checkout -b feature/story-name
```

### Step 2: Implement Story
- Backend: Service + Controller + Tests
- Frontend: Component + Tests
- Update docs if needed

### Step 3: Local Testing
```bash
# Backend
cd main/backend
mvn -pl services/ai-service test
mvn -pl services/ai-service spring-boot:run

# Frontend
cd main/frontend
npm test
npm run dev
```

### Step 4: Commit Changes
```bash
git add .
git commit -m "feat(ai-service): Story X description

- Detailed change 1
- Detailed change 2
- Detailed change 3"
```

### Step 5: Push & Create PR
```bash
git push origin feature/story-name
# Create PR on GitHub
```

### Step 6: Merge to Main
```bash
git checkout main
git pull origin main
git merge --no-ff feature/story-name
git push origin main
git branch -d feature/story-name
```

---

## 📈 IMPLEMENTATION TIMELINE

| Week | Sprint | Deliverable | Status |
|------|--------|-------------|--------|
| **W1** | Sprint 1 | AI Core MVP (Backend) | ✅ Implemented |
| **W1-W2** | Sprint 1 | Chat UI (Frontend) | ✅ Implemented |
| **W2-W3** | Sprint 2 | Structured Input Form | ✅ Implemented |
| **W3** | Sprint 2 | Dynamic Prompt (BE) | ✅ Implemented |
| **W3-W4** | Sprint 3 | Admin Prompt Management | ✅ Implemented |
| **W4** | Post-Task3 | Error Analysis & Fixes | ▶️ Next |
| **W4-W5** | Post-Task3 | Risk Analysis & Hardening | ▶️ Planned |

**Total Duration:** 4-5 weeks

---

## ✅ DEFINITION OF DONE

### Backend Story
- [ ] Code compiles & runs
- [ ] Unit tests pass (min 80% coverage)
- [ ] Integration tests pass
- [ ] API contract documented
- [ ] Postman collection updated
- [ ] Health endpoint works
- [ ] No hardcoded secrets
- [ ] Flyway migration created (if DB change)
- [ ] Commit message descriptive
- [ ] PR reviewed & approved

### Frontend Story
- [ ] Components render without errors
- [ ] Responsive on desktop & mobile
- [ ] All loading/error states implemented
- [ ] Form validation working
- [ ] CSS Modules properly used
- [ ] No console errors/warnings
- [ ] Accessibility basics (alt text, labels)
- [ ] Commit message descriptive
- [ ] PR reviewed & approved

---

## 🚨 RISKS & MITIGATION

| Risk | Level | Impact | Mitigation |
|------|-------|--------|-----------|
| AI API rate limited | 🔴 High | Request rejected | Use free tier quota (15 req/min), add queue if needed |
| AI returns invalid JSON | 🟠 Medium | Parser crashes | Implement robust fallback parser |
| Timeout > 2s | 🟠 Medium | Poor UX | Retry 1x, timeout error message |
| UI layout broken | 🟢 Low | Mobile issues | Test on device, CSS modules validation |
| Database migration fails | 🔴 High | Deployment blocked | Test migration locally, backup before prod |
| Prompt injection vulnerability | 🔴 High | Security risk | Sanitize input, whitelist variables, escape user input |

---

## 📝 TESTING STRATEGY

### Sprint 1 Testing
- **Unit Test:** Parser, Formatter
- **Integration Test:** AIClient → Parser → Formatter
- **E2E Test:** Postman collection (manual)

### Sprint 2 Testing
- **Unit Test:** PromptBuilder, StructuredInputValidator
- **Integration Test:** Full flow with mock AI

### Sprint 3 Testing
- **Unit Test:** PromptTemplateService, DynamicPromptBuilder
- **Integration Test:** DB operations, CRUD API
- **E2E Test:** Admin UI workflow

---

## 📊 SUCCESS METRICS

- ✅ All 3 sprints completed on time
- ✅ Zero critical bugs in production
- ✅ AI response accuracy > 70% (user feedback)
- ✅ Average response time < 2s
- ✅ Chat UI used by > 50% users
- ✅ Zero prompt injection incidents

---

## 🎯 NEXT STEPS (AFTER TASK 3)

### Phase 1: Error Analysis (1 week)
- Bug hunting & test matrix
- User feedback collection
- Performance profiling
- Security audit

### Phase 2: Fixes & Hardening (1 week)
- Fix identified bugs
- Implement missing validations
- Optimize performance
- Enhanced error messages

### Phase 3: Risk Analysis (1 week)
- Operational risks
- Deployment risks
- Scaling considerations
- Disaster recovery plan

---

## 📞 CONTACT & SUPPORT

**Product Owner:** [TBD]  
**Tech Lead:** [TBD]  
**QA Lead:** [TBD]  

---

**Document Version:** 1.0  
**Last Updated:** 2026-04-29  
**Next Review:** Post-implementation validation
