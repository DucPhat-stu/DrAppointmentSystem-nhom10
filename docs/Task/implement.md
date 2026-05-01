# AI-Service: Kiểm tra, Sửa lỗi & Phân tích Rủi ro

## 📋 Tổng quan

Thực hiện kiểm tra toàn diện `ai-service` dựa trên Task 3 (3 Sprints: AI Core MVP → Structured Input → Admin Prompt Management), xác định lỗi, sửa lỗi, và phân tích rủi ro theo mức độ.

---

## ✅ KẾT QUẢ KIỂM TRA BUILD & TEST

### Compilation
- **Maven compile**: ✅ BUILD SUCCESS — Không có lỗi biên dịch
- **27 source files** compiled thành công (Java 21)
- **Shared modules** (api-contract, security-contract, common): Tất cả compile OK

### Unit Tests
- **18 / 18 test PASS**, 0 failure, 0 error
- Coverage theo class:

| Test Class | Tests | Status |
|---|---|---|
| AIClientTest | 1 | ✅ Pass |
| AIConversationServiceTest | 2 | ✅ Pass |
| AIInputSanitizerTest | 2 | ✅ Pass |
| AIPromptBuilderTest | 2 | ✅ Pass |
| AIResponseParserTest | 5 | ✅ Pass |
| AITextFormatterTest | 3 | ✅ Pass |
| DynamicPromptBuilderTest | 2 | ✅ Pass |
| PromptTemplateServiceTest | 1 | ✅ Pass |

---

## 🐛 LỖI & VẤN ĐỀ PHÁT HIỆN

### BUG-01: Timeout quá thấp (2000ms) cho Gemini API ⚠️ TRUNG BÌNH

**File:** [application.yml](file:///c:/Users/Phat/OneDrive/Máy tính/TKHT/main/backend/services/ai-service/src/main/resources/application.yml#L51)

```yaml
ai:
  timeout-ms: ${AI_TIMEOUT_MS:2000}
```

**Vấn đề:** Gemini API free tier thường mất 1.5–3s để trả response. Timeout 2s sẽ khiến nhiều request hợp lệ bị timeout, trigger fallback liên tục. Kết hợp với retry 1 lần trong `AIClient.generate()`, tổng thời gian chờ tối đa chỉ là 4s — vẫn có thể gây UX kém.

**Đề xuất sửa:** Tăng timeout lên 5000ms (hoặc ít nhất 3000ms).

```diff
ai:
-  timeout-ms: ${AI_TIMEOUT_MS:2000}
+  timeout-ms: ${AI_TIMEOUT_MS:5000}
```

---

### BUG-02: AIClient thiếu Content-Type header khi gọi Gemini API ⚠️ TRUNG BÌNH

**File:** [AIClient.java](file:///c:/Users/Phat/OneDrive/Máy tính/TKHT/main/backend/services/ai-service/src/main/java/com/healthcare/ai/service/AIClient.java#L50-L54)

```java
String body = restClient.post()
        .uri(geminiUrl())
        .body(requestBody(prompt))
        .retrieve()
        .body(String.class);
```

**Vấn đề:** Không set `Content-Type: application/json` header. `RestClient` gửi Map object → cần `contentType(MediaType.APPLICATION_JSON)` để đảm bảo serialization đúng. Dù Spring có thể tự infer, đây là implicit behavior có thể thất bại với cấu hình HTTP client khác.

**Đề xuất sửa:**
```diff
 String body = restClient.post()
         .uri(geminiUrl())
+        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
         .body(requestBody(prompt))
         .retrieve()
         .body(String.class);
```

---

### BUG-03: Không có logging trong AIClient — khó debug production 🟢 THẤP

**File:** [AIClient.java](file:///c:/Users/Phat/OneDrive/Máy tính/TKHT/main/backend/services/ai-service/src/main/java/com/healthcare/ai/service/AIClient.java#L57-L61)

```java
} catch (RestClientException | IllegalArgumentException exception) {
    if (attempt == 1) {
        return FALLBACK_JSON;
    }
}
```

**Vấn đề:** Exception bị swallow hoàn toàn, không log. Khi production gặp vấn đề (API key hết hạn, rate limit, network issue), sẽ không có log nào để debug. Tất cả đều trả về fallback "im lặng".

**Đề xuất sửa:**
```diff
+private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AIClient.class);
 ...
 } catch (RestClientException | IllegalArgumentException exception) {
+    log.warn("Gemini API call attempt {} failed: {}", attempt + 1, exception.getMessage());
     if (attempt == 1) {
+        log.error("Gemini API failed after 2 attempts, returning fallback response");
         return FALLBACK_JSON;
     }
 }
```

---

### BUG-04: PromptTemplateService.activate() — N+1 query problem ⚠️ TRUNG BÌNH

**File:** [PromptTemplateService.java](file:///c:/Users/Phat/OneDrive/Máy tính/TKHT/main/backend/services/ai-service/src/main/java/com/healthcare/ai/service/PromptTemplateService.java#L80-L88)

```java
public PromptTemplateResponse activate(UUID id) {
    PromptTemplateEntity target = find(id);
    repository.findAll().forEach(template -> {
        template.setActive(false);
        repository.save(template);   // <-- N individual UPDATE queries!
    });
    target.setActive(true);
    return toResponse(repository.save(target));
}
```

**Vấn đề:** Load TẤT CẢ template rồi update từng cái một. Nếu có 100 template → 100 UPDATE query + 1 SELECT. Nên dùng 1 batch UPDATE.

**Đề xuất sửa:** Thêm query vào repository:
```java
// PromptTemplateJpaRepository.java
@Modifying
@Query("UPDATE PromptTemplateEntity t SET t.active = false WHERE t.active = true")
void deactivateAll();
```

Rồi dùng:
```java
public PromptTemplateResponse activate(UUID id) {
    PromptTemplateEntity target = find(id);
    repository.deactivateAll();
    target.setActive(true);
    return toResponse(repository.save(target));
}
```

---

### BUG-05: Thiếu Rate Limiting — có thể bị abuse 🔴 CAO

**File:** [SecurityConfig.java](file:///c:/Users/Phat/OneDrive/Máy tính/TKHT/main/backend/services/ai-service/src/main/java/com/healthcare/ai/config/SecurityConfig.java) & [AIController.java](file:///c:/Users/Phat/OneDrive/Máy tính/TKHT/main/backend/services/ai-service/src/main/java/com/healthcare/ai/web/AIController.java)

**Vấn đề:** Không có rate limiting ở backend. Frontend có cooldown 2s nhưng attacker có thể bypass bằng cách gọi API trực tiếp, dẫn đến:
1. Burn hết Gemini API quota (free tier: 15 req/min)
2. Gây cost spike nếu dùng paid tier
3. DoS cho users khác

**Đề xuất:** Thêm rate limiter (Bucket4j hoặc Resilience4j).

---

### BUG-06: API key lộ trong URL query parameter 🔴 CAO

**File:** [AIClient.java](file:///c:/Users/Phat/OneDrive/Máy tính/TKHT/main/backend/services/ai-service/src/main/java/com/healthcare/ai/service/AIClient.java#L74-L82)

```java
private String geminiUrl() {
    return "%s/models/%s:generateContent?key=%s".formatted(
            geminiProperties.getBaseUrl(), model, apiKey
    );
}
```

**Vấn đề:** API key được truyền qua URL query string. Nếu có bất kỳ HTTP access log, proxy log, hoặc error log nào ghi lại URL, API key sẽ bị lộ. Đây là thiết kế của Google Gemini API (bắt buộc key trong URL), nhưng cần đảm bảo:
- Không log URL ở level DEBUG
- Không enable Spring Boot `logging.level.org.springframework.web=DEBUG`

**Đề xuất:** Thêm cảnh báo trong config & đảm bảo request logging KHÔNG log URL params.

---

### BUG-07: JWT secret mặc định quá yếu 🔴 CAO (nếu deploy production)

**File:** [application.yml](file:///c:/Users/Phat/OneDrive/Máy tính/TKHT/main/backend/services/ai-service/src/main/resources/application.yml#L55)

```yaml
security:
  jwt:
    secret: ${JWT_SECRET:change-this-before-shared-environments}
```

**Vấn đề:** Default secret quá ngắn và predictable. Nếu deploy mà quên set `JWT_SECRET` env var, bất kỳ ai cũng có thể forge JWT token.

**Đề xuất:** Thêm startup validation — fail fast nếu secret là default value.

---

### BUG-08: Preview endpoint thiếu security check ⚠️ TRUNG BÌNH

**File:** [AIController.java](file:///c:/Users/Phat/OneDrive/Máy tính/TKHT/main/backend/services/ai-service/src/main/java/com/healthcare/ai/web/AIController.java#L41-L47)

```java
@PostMapping("/preview/structured")
public ApiResponse<String> previewStructuredPrompt(@Valid @RequestBody StructuredAICheckRequest request) {
```

**Vấn đề:** Endpoint `/api/v1/ai/preview/structured` cho phép bất kỳ user authenticated nào xem full prompt sẽ gửi cho AI. Prompt template có thể chứa system instructions nhạy cảm. Nên restrict cho ADMIN.

---

### BUG-09: Thiếu Integration Test và Controller Test 🟢 THẤP

**Vấn đề:** Chỉ có unit test cho service layer. Không có test nào cover:
- Controller layer (HTTP status codes, validation errors)
- Security filter (JWT authentication)
- Full integration flow (Controller → Service → AIClient)
- Database operations (PromptTemplateService CRUD)

---

## 📊 PHÂN TÍCH RỦI RO

### 🔴 RỦI RO CAO (Critical — Cần xử lý ngay)

| # | Rủi ro | Mô tả | Xác suất | Tác động | Giải pháp |
|---|---|---|---|---|---|
| R1 | **Prompt Injection** | User gửi text chứa "Ignore previous instructions..." → AI làm theo lệnh attacker, trả về nội dung sai/nguy hiểm | Trung bình | Rất cao | `AIInputSanitizer` hiện chỉ strip HTML/control chars. Cần thêm detect prompt injection patterns, hoặc wrap user input trong delimiters rõ ràng (```User input: "..."```) |
| R2 | **Lộ API Key** | API key trong URL params có thể bị log bởi proxy, WAF, access log | Cao | Cao | Disable request URL logging, audit log config, rotate key định kỳ |
| R3 | **Không có Rate Limiting** | Attacker spam API → burn hết Gemini quota → DoS cho tất cả users | Cao | Cao | Thêm rate limiter per-user (10 req/min), thêm global rate limit (100 req/min) |
| R4 | **JWT Secret mặc định** | Production quên set JWT_SECRET → attacker forge token → truy cập bất kỳ API nào | Thấp | Rất cao | Thêm startup check: nếu secret = default → throw exception, application không start |
| R5 | **AI trả thông tin y tế sai** | AI hallucinate → user tin và không đi khám → nguy hiểm sức khỏe | Trung bình | Rất cao | Disclaimer đã có ✅. Cần thêm: không hiển thị tên thuốc cụ thể, luôn khuyến khích gặp bác sĩ, thêm "Was this helpful?" feedback |
| R6 | **Database migration fail** | Flyway migration lỗi → service không start | Thấp | Cao | Test migration trước deploy, backup DB, có rollback script |

---

### 🟠 RỦI RO TRUNG BÌNH (Important — Nên xử lý trong sprint tiếp)

| # | Rủi ro | Mô tả | Xác suất | Tác động | Giải pháp |
|---|---|---|---|---|---|
| R7 | **Gemini API Rate Limit** | Free tier 15 req/min. Với 10+ users đồng thời → quota hết → tất cả nhận fallback | Cao | Trung bình | Monitor quota, cache response (Redis), upgrade plan, hoặc sử dụng multiple API keys |
| R8 | **Timeout cascade** | Gemini API chậm → request queue up → thread pool exhaustion → service unresponsive | Trung bình | Trung bình | Sử dụng async processing hoặc thread pool riêng cho AI calls, implement circuit breaker |
| R9 | **Template injection qua Admin** | Admin tạo template có placeholders lạ → DynamicPromptBuilder render sai → AI nhận prompt bị corrupt | Trung bình | Trung bình | Validation đã có ✅ (kiểm tra unresolved `{{}}`). Thêm: whitelist chỉ cho phép `symptoms`, `duration`, `description` |
| R10 | **N+1 query khi activate template** | `activate()` load all + update từng entity → performance kém khi nhiều templates | Thấp | Trung bình | Thêm batch UPDATE query trong repository |
| R11 | **Thiếu audit logging** | Không có log ai đã tạo/sửa/xóa template nào, khi nào | Trung bình | Trung bình | Thêm `createdBy`, `updatedBy` vào entity, log CRUD operations |
| R12 | **CORS chỉ cho localhost** | Production deploy sẽ fail vì CORS chỉ allow `localhost:5173` và `localhost:3000` | Cao (deploy) | Trung bình | Config CORS từ environment variable |
| R13 | **Không có health check cho Gemini API** | `/actuator/health` chỉ check service + DB, không check AI API connectivity | Trung bình | Trung bình | Thêm custom health indicator cho Gemini API reachability |

---

### 🟢 RỦI RO THẤP (Minor — Theo dõi, xử lý khi có thời gian)

| # | Rủi ro | Mô tả | Xác suất | Tác động | Giải pháp |
|---|---|---|---|---|---|
| R14 | **Memory leak từ RestClient** | `SimpleClientHttpRequestFactory` mở connections nhưng không có connection pooling → memory leak dưới load cao | Thấp | Thấp | Chuyển sang `HttpComponentsClientHttpRequestFactory` với connection pool |
| R15 | **Thiếu metrics/monitoring** | Không có Prometheus metrics → không biết response time, error rate | Trung bình | Thấp | Thêm Micrometer + `/actuator/prometheus` endpoint |
| R16 | **Thiếu pagination cho template list** | `GET /api/v1/ai/templates` trả TẤT CẢ templates | Thấp | Thấp | Thêm Pageable parameter |
| R17 | **Thiếu request ID correlation** | Log pattern có traceId nhưng không generate/propagate → log khó trace | Thấp | Thấp | Thêm MDC filter cho request correlation |
| R18 | **FE cooldown dễ bypass** | 2s cooldown chỉ ở client-side → bypassed bằng Postman/curl | Cao | Thấp | Backend rate limiting (R3) sẽ giải quyết |
| R19 | **Không có input length indicator** | FE có `maxLength` nhưng không hiển thị "245/500 ký tự" | Thấp | Thấp | Thêm character counter component |
| R20 | **Không có chat history persistence** | Chat mất khi reload page | Thấp | Thấp | Dự kiến không cần cho MVP |

---

## 🎯 ĐỀ XUẤT HÀNH ĐỘNG (Ưu tiên)

### Giai đoạn 1 — Sửa ngay (1-2 ngày)
1. ✅ **BUG-03:** Thêm logging vào AIClient
2. ✅ **BUG-01:** Tăng timeout lên 5s
3. ✅ **BUG-02:** Thêm Content-Type header
4. ✅ **BUG-04:** Fix N+1 query trong activate()

### Giai đoạn 2 — Hardening (3-5 ngày)
5. ⚠️ **BUG-05 / R3:** Thêm rate limiting
6. ⚠️ **BUG-07 / R4:** Validate JWT secret on startup
7. ⚠️ **BUG-08:** Restrict preview endpoint cho ADMIN
8. ⚠️ **R1:** Cải thiện prompt injection protection

### Giai đoạn 3 — Production readiness (1 tuần)
9. 📊 **R7:** Implement response caching (Redis)
10. 📊 **R8:** Implement circuit breaker
11. 📊 **R12:** Config CORS từ environment
12. 📊 **R13:** Custom health indicator cho Gemini

---

## Open Questions

> [!IMPORTANT]
> **Q1:** Bạn muốn tôi thực hiện sửa lỗi ngay (Giai đoạn 1 — BUG-01 đến BUG-04), hay bạn muốn review phân tích trước rồi mới sửa?

> [!IMPORTANT]  
> **Q2:** Bạn có Gemini API key để test runtime không? (build & unit test đã pass, nhưng runtime test cần PostgreSQL + API key)
