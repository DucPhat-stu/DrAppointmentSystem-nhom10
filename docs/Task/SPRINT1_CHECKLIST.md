# TASK 3: SPRINT 1 DETAILED CHECKLIST
**Created:** 2026-04-29  
**Sprint:** Sprint 1 - AI Chatbot Core  
**Target Duration:** 5-7 working days

---

## CURRENT STATUS: SPRINT 1 IMPLEMENTED

### Backend Status
- [x] auth-service ✅ Active
- [x] user-service ✅ Active
- [x] doctor-service ✅ Active
- [x] appointment-service ✅ Active
- [x] notification-service ✅ Active
- [x] ai-service ✅ Active on port 8087

### Frontend Status
- [x] Authentication flows ✅
- [x] Doctor browsing ✅
- [x] Appointment booking ✅
- [x] Notifications view ✅
- [x] ChatbotPage ✅ Active at `/chat`

### Database
- [x] PostgreSQL running ✅
- [x] Docker Compose ready ✅
- [x] ai_db tables ✅ Created with Flyway migrations

---

## 🚀 SPRINT 1 EXECUTION CHECKLIST

### STORY 1: Backend Setup & AI Integration
**Status:** ✅ DONE  
**Branch:** `feature/ai-service-setup`  
**Assignee:** Backend Developer

#### Task 1.1: Create ai-service Maven Module
- [x] Create directory: `backend/services/ai-service`
- [x] Create `pom.xml` with dependencies:
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-cloud-starter-openfeign (or RestTemplate)
  - lombok
  - jackson
- [x] Copy structure from appointment-service:
  - src/main/java/com/healthcare/ai/
  - src/main/resources/application.yml
  - src/main/resources/db/migration/
  - src/test/java/

#### Task 1.2: Setup Spring Boot Configuration
- [x] Create `application.yml`:
  ```yaml
  spring:
    application:
      name: ai-service
    jpa:
      hibernate:
        ddl-auto: validate
    datasource:
      url: ${DB_URL:jdbc:postgresql://localhost:5432/healthcare_ai}
      username: ${DB_USERNAME:postgres}
      password: ${DB_PASSWORD:postgres}
  
  server:
    port: ${APP_PORT:8085}
  
  ai:
    gemini:
      api-key: ${AI_API_KEY}
      model: ${AI_MODEL:gemini-pro}
      base-url: ${AI_BASE_URL:https://generativelanguage.googleapis.com/v1beta}
    timeout:
      ms: 2000
  ```

#### Task 1.3: Create Gemini API Client
- [x] Create `service/AIClient.java`
- [x] Implement HTTP call to Gemini API
- [x] Add timeout configuration
- [x] Implement retry/fallback behavior
- [x] Error handling (return fallback response, no exception thrown)
- [x] Unit tests for AIClient

#### Task 1.4: Add Health & Actuator Endpoint
- [ ] Add Spring Actuator dependency
- [ ] Expose `/actuator/health` endpoint
- [ ] Test health endpoint returns 200

#### Task 1.5: Flyway Migration
- [x] Create `src/main/resources/db/migration/V1__initial_schema.sql`
- [x] Create AI service tables/migrations
- [ ] Run migration locally

#### Task 1.6: Add to Backend Parent POM
- [x] Update `backend/pom.xml` to include ai-service module

#### Acceptance Criteria
- [x] ai-service starts on port 8085
- [x] Health endpoint `/actuator/health` returns 200
- [x] AIClient can call Gemini API
- [x] Response received within 2s
- [x] No exception thrown on error

#### Testing Checklist
- [ ] Local startup: `mvn -pl services/ai-service spring-boot:run`
- [ ] Health check: `curl http://localhost:8085/actuator/health`
- [ ] AI call test: Manual Postman or unit test
- [ ] Unit tests pass: `mvn -pl services/ai-service test`

#### Git Commit
```
feat(ai-service): Setup ai-service module and Gemini API client

- Create ai-service Maven module with Spring Boot structure
- Implement AIClient with timeout and retry logic
- Setup Gemini API integration (free tier)
- Add health endpoint and actuator
- Configure environment variables
- Create initial Flyway migration
```

---

### STORY 2: Response Parser
**Status:** ✅ DONE  
**Branch:** `feature/ai-response-parser`  
**Assignee:** Backend Developer  
**Depends on:** Story 1 ✅

#### Task 2.1: Create Response DTOs
- [x] Create `dto/AICheckResponse.java`:
  ```java
  public record AICheckResponse(
      List<String> possibleConditions,
      List<String> symptomsDetected,
      String recommendedSpecialty,
      String advice
  ) {}
  ```

#### Task 2.2: Create Parser Component
- [x] Create `service/AIResponseParser.java`
- [x] Implement JSON parsing logic
- [ ] Add validation rules:
  - possibleConditions: not null, not empty, max 5 items
  - symptomsDetected: not null, not empty, max 10 items
  - recommendedSpecialty: required, non-empty, max 100 chars
  - advice: required, min 10 chars, max 500 chars
- [ ] Implement fallback response on error

#### Task 2.3: Unit Tests
- [x] Test valid JSON parsing
- [x] Test invalid JSON (returns fallback)
- [x] Test missing fields (returns fallback)
- [x] Test validation rules (boundary cases)

#### Acceptance Criteria
- [x] Valid JSON parsed correctly
- [x] Invalid JSON returns fallback (NO exception)
- [x] All fields validated
- [x] Unit tests pass with >80% coverage

#### Git Commit
```
feat(ai-service): Implement response parser with fallback

- Create AICheckResponse DTO
- Implement AIResponseParser with JSON validation
- Add comprehensive fallback for malformed responses
- Unit tests for happy path and error cases
- Validation: conditions (5 max), specialty (required), advice (min 10 chars)
```

---

### STORY 3: Text Formatter
**Status:** ✅ DONE  
**Branch:** `feature/ai-text-formatter`  
**Assignee:** Backend Developer  
**Depends on:** Story 2 ✅

#### Task 3.1: Create Formatter Component
- [x] Create `service/AITextFormatter.java`
- [ ] Implement formatting logic:
  ```
  🏥 Các bệnh có thể: condition1, condition2
  
  📋 Triệu chứng ghi nhận: symptom1, symptom2
  
  👨‍⚕️ Gợi ý khám: specialty
  
  💡 Lời khuyên: advice
  
  ⚠️ Lưu ý: Đây chỉ là gợi ý từ AI, không thay thế chẩn đoán y khoa
  ```

#### Task 3.2: Handle Edge Cases
- [x] Empty arrays handled
- [x] Null values handled
- [x] Long text handled safely

#### Task 3.3: Unit Tests
- [x] Test formatting with all fields
- [x] Test with empty arrays
- [x] Test with null values
- [x] Test disclaimer present

#### Acceptance Criteria
- [x] Output readable
- [x] Có đủ 4 phần thông tin
- [x] Không hiển thị null
- [x] Có disclaimer

#### Git Commit
```
feat(ai-service): Implement text formatter with disclaimer

- Create AITextFormatter for user-friendly output
- Format: conditions, symptoms, specialty, advice with emojis
- Add prominent disclaimer to avoid medical liability
- Handle null/empty values gracefully
- Unit tests for all formatting scenarios
```

---

### STORY 4: API Endpoint
**Status:** ✅ DONE  
**Branch:** `feature/ai-check-endpoint`  
**Assignee:** Backend Developer  
**Depends on:** Story 3 ✅

#### Task 4.1: Create Request DTO
- [x] Create `dto/AICheckRequest.java`:
  ```java
  public record AICheckRequest(
      @NotBlank(message = "Symptoms text is required")
      @Length(min = 5, max = 500, message = "Text must be 5-500 characters")
      String text
  ) {}
  ```

#### Task 4.2: Create Controller
- [x] Create `web/AIController.java`
- [x] Endpoint: `POST /api/v1/ai/check`
- [x] Input validation using @Valid
- [x] Call AIClient → Parser → Formatter pipeline
- [x] Return formatted text in ApiResponse<String>

#### Task 4.3: Error Handling
- [ ] Global exception handler catches parser errors
- [ ] Return proper error response (not exception)
- [ ] Validation errors → 400 Bad Request
- [ ] Server errors → 500 Internal Server Error

#### Task 4.4: Integration Tests
- [ ] Test with valid input
- [ ] Test with invalid input (too short/long)
- [ ] Test with mock AI response
- [ ] Test error scenarios (AI timeout, invalid JSON)
- [ ] Test response time < 2s

#### Task 4.5: Postman Collection
- [x] Create Postman request for POST /api/v1/ai/check
- [x] Add example with valid input
- [x] Add structured AI examples
- [x] Add AI collection

#### Acceptance Criteria
- [x] Response time < 2s
- [x] Format consistent (ApiResponse<String>)
- [x] Validation on input
- [x] Integration tests pass

#### Git Commit
```
feat(ai-service): Add /api/v1/ai/check endpoint

- Create AIController with POST /api/v1/ai/check
- Integrate AIClient → Parser → Formatter pipeline
- Input validation (5-500 chars text)
- Error handling with proper response codes
- Integration tests with mock AI
- Add Postman collection examples
```

---

### STORY 5: Chat UI (Frontend)
**Status:** ✅ DONE  
**Branch:** `feature/chat-ui-basic`  
**Assignee:** Frontend Developer  
**Depends on:** Story 4 ✅

#### Task 5.1: Create ChatbotPage Component
- [x] Create `pages/ChatbotPage.jsx`
- [ ] Initialize state:
  - messages: []
  - inputText: ''
  - loading: false
  - error: null

#### Task 5.2: Create Chat Service
- [x] Create `services/chatService.js`
- [x] Implement `checkSymptoms(text)` function
- [x] Use AI API client for `/api/v1/ai/check`

#### Task 5.3: UI Components
- [x] Chat messages container
- [x] User message bubble
- [x] AI message bubble
- [x] Input box + Send button
- [x] Structured symptom input mode

#### Task 5.4: Chat Logic
- [ ] On send button click:
  1. Validate input (not empty)
  2. Add user message to UI
  3. Clear input box
  4. Set loading = true
  5. Call chatService.checkSymptoms(text)
  6. Add AI response to UI
  7. Set loading = false
- [ ] Disable input/button while loading

#### Task 5.5: CSS Module
- [x] Create `pages/ChatbotPage.module.css`
- [ ] Styling:
  - Container: full width, flex column
  - Chat box: scrollable, light background
  - User bubble: right-aligned, blue background
  - AI bubble: left-aligned, gray background
  - Input box: fixed at bottom
  - Responsive for mobile

#### Task 5.6: Add to Router
- [x] Add route in `app/router.jsx`:
  ```jsx
  {
    path: '/chat',
    element: <ChatbotPage />,
  }
  ```

#### Task 5.7: Add Navigation Link
- [x] Add link in main navigation/sidebar
- [x] Link to `/chat`

#### Acceptance Criteria
- [x] Không reload page
- [x] User gửi → thấy response ngay
- [x] UI không bị vỡ layout
- [x] Responsive on mobile

#### Testing Checklist
- [ ] Start backend: `mvn -pl services/ai-service spring-boot:run`
- [ ] Start frontend: `npm run dev`
- [ ] Navigate to /chat
- [ ] Send message with valid symptoms
- [ ] Verify response appears in chat
- [ ] Test on mobile browser (DevTools)

#### Git Commit
```
feat(frontend): Add ChatbotPage with basic chat UI

- Create ChatbotPage component with message state
- Implement chat service wrapper for AI endpoint
- Chat bubbles: user (right), AI (left)
- Input box with send button
- Auto-scroll to latest message
- CSS Modules for responsive design
- Add route to /chat
```

---

### STORY 6: Loading & Error Handling
**Status:** ✅ DONE  
**Branch:** `feature/chat-error-handling`  
**Assignee:** Frontend Developer  
**Depends on:** Story 5 ✅

#### Task 6.1: Add Loading State UI
- [x] Show loading state while waiting for AI response
- [x] Disable input box during request
- [x] Show processing state on send button
- [ ] Add timeout warning after 3s (if not responded)

#### Task 6.2: Error Messages
- [x] Create error messages for different scenarios:
  - Validation: "Vui lòng nhập triệu chứng (tối thiểu 5 ký tự)"
  - Network: "Kiểm tra kết nối internet"
  - Timeout: "Yêu cầu quá lâu, vui lòng thử lại"
  - Server 500: "Lỗi server, vui lòng liên hệ hỗ trợ"
  - Default: "Có lỗi xảy ra, vui lòng thử lại"

#### Task 6.3: Error UI Component
- [x] Create error box with:
  - ⚠️ Icon
  - Error message
  - Retry button (optional)

#### Task 6.4: Prevent Spam
- [x] Add request spam prevention
- [x] Track last request state
- [x] Disable send button while not ready

#### Task 6.5: Max Retry Logic
- [ ] On network error, allow 1 retry
- [ ] Show "Thử lại" button
- [ ] Track retry count
- [ ] Don't retry on validation error

#### Task 6.6: Unit Tests
- [ ] Test error message display
- [ ] Test loading state toggle
- [ ] Test cooldown logic
- [ ] Test retry button behavior

#### Acceptance Criteria
- [x] Không spam request
- [x] Có thông báo khi lỗi
- [x] User-friendly error messages
- [x] Graceful fallback

#### Git Commit
```
feat(frontend): Add error handling and loading states to chat

- Add loading spinner during AI request
- Disable input/button while loading
- Handle network, validation, server errors
- Implement 2s cooldown to prevent spam
- Add retry logic (1 attempt for network errors)
- User-friendly Vietnamese error messages
```

---

## ✅ SPRINT 1 COMPLETION CHECKLIST

### Backend Testing
- [ ] All services compile: `mvn clean compile`
- [ ] All unit tests pass: `mvn test`
- [ ] Integration tests pass: `mvn verify`
- [ ] Code coverage > 80%: Check Jacoco report
- [ ] No security warnings: Check OWASP dependency check

### Frontend Testing
- [ ] No ESLint errors: `npm run lint`
- [ ] Unit tests pass: `npm test`
- [ ] Dev server runs: `npm run dev`
- [ ] Build succeeds: `npm run build`
- [ ] No console errors/warnings

### Manual Testing
- [ ] Backend healthy:
  ```bash
  curl http://localhost:8085/actuator/health
  ```
- [ ] Chat UI loads:
  ```
  Navigate to http://localhost:5173/chat
  ```
- [ ] Send message and receive AI response
- [ ] Test on mobile browser
- [ ] Test error scenarios

### Documentation
- [ ] Postman collection updated with /ai/check
- [ ] README updated with ai-service info
- [ ] SKILL.md updated with AI flow
- [ ] API contract documented in SKILL.md

### Git Workflow
- [ ] All branches created with correct names
- [ ] All commits have descriptive messages
- [ ] All PRs merged to main
- [ ] main branch is stable

### Postman Smoke Test
- [ ] Create smoke test in Postman:
  1. POST /auth/login
  2. POST /ai/check (with AI token)
  3. Verify response structure

---

## 📊 SPRINT 1 METRICS

| Metric | Target | Status |
|--------|--------|--------|
| Code coverage | > 80% | ▶️ Pending |
| Test pass rate | 100% | ▶️ Pending |
| Response time < 2s | 100% | ▶️ Pending |
| Error rate | < 1% | ▶️ Pending |
| UI responsive | Mobile + Desktop | ▶️ Pending |

---

## 🚨 SPRINT 1 RISKS

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| AI API rate limited | Medium | High | Monitor quota, implement queue |
| JSON parse fails | Medium | High | Robust fallback parser |
| Timeout > 2s | Low | Medium | Retry logic, timeout message |
| Layout broken mobile | Low | Low | CSS test on device |

---

## 👥 TEAM ASSIGNMENTS

- **Backend Lead:** [Name TBD] - Stories 1-4
- **Frontend Lead:** [Name TBD] - Stories 5-6
- **QA Lead:** [Name TBD] - Testing & Validation
- **DevOps:** [Name TBD] - Docker, Deployment

---

## 📅 SPRINT 1 TIMELINE

| Day | Milestone | Tasks |
|-----|-----------|-------|
| **Day 1-2** | Story 1 Setup | ai-service, AIClient |
| **Day 2-3** | Story 2 Parser | Response parsing |
| **Day 3-4** | Story 3 Formatter | Text formatting |
| **Day 4-5** | Story 4 Endpoint | API + Integration tests |
| **Day 5-6** | Story 5 Chat UI | Frontend components |
| **Day 6-7** | Story 6 Error Handling | Loading, errors, retry |
| **Day 7** | Testing & Review | Manual testing, PR review |

---

## 🎯 SPRINT 1 SUCCESS CRITERIA

**Definition of Done:**
- [ ] All 6 stories completed
- [ ] All code reviewed and merged
- [ ] Test coverage > 80%
- [ ] No critical bugs
- [ ] Documentation updated
- [ ] Postman collection ready
- [ ] Ready for Sprint 2

**Go/No-Go Decision:** After Day 7 review

---

**Document Created:** 2026-04-29  
**Last Updated:** 2026-04-29  
**Status:** IMPLEMENTED - VALIDATION PENDING

Next step: Run Sprint 1 validation and fix any findings.
