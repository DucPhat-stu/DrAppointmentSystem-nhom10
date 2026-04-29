# POST-TASK3: ERROR ANALYSIS, TESTING & OPERATIONAL RISKS
**Created:** 2026-04-29  
**Purpose:** Roadmap for after Task3 completion  
**Duration:** 2-3 weeks  

---

## 📋 OVERVIEW

Sau khi hoàn thành Task3 (3 sprints), sẽ thực hiện:
1. **Phase 1 (Week 1):** Error Analysis & Bug Hunt
2. **Phase 2 (Week 1-2):** Fix & Hardening
3. **Phase 3 (Week 2-3):** Operational Risk Analysis

---

## 🔍 PHASE 1: ERROR ANALYSIS & BUG HUNT (5 working days)

### 1.1 Unit Test Coverage Analysis
**Objective:** Identify gaps in unit test coverage

#### Tasks
- [ ] Generate coverage report: `mvn jacoco:report`
- [ ] Analyze coverage by module:
  - ai-service (BE): Target > 80%
  - Components (FE): Target > 70%
- [ ] Identify untested code paths
- [ ] Create coverage improvement plan

#### Coverage Areas to Check
```
Backend:
- AIClient (network, timeout, retry logic)
- AIResponseParser (valid/invalid JSON, edge cases)
- AITextFormatter (null handling, formatting)
- AIController (validation, error handling)
- Service layer integration

Frontend:
- ChatbotPage component (render, state management)
- Chat service (API calls, error handling)
- Input validation (empty, too long)
- Loading/error states
- Event handlers
```

#### Acceptance Criteria
- [x] Coverage report generated
- [x] All critical paths identified
- [x] Improvement plan documented

---

### 1.2 Integration Test Matrix
**Objective:** Identify missing integration scenarios

#### Test Scenarios
- [ ] **Happy Path:** User → UI → AI API → Response → UI
- [ ] **Network Error:** AI API returns 429 (rate limited)
- [ ] **Timeout:** AI API doesn't respond within 2s
- [ ] **Invalid JSON:** AI API returns malformed JSON
- [ ] **Validation Error:** Input too short/long
- [ ] **Server Error:** 500 Internal Server Error
- [ ] **Circuit Breaker:** AI API down, fallback response
- [ ] **Concurrent Requests:** Multiple users chat simultaneously
- [ ] **Database Error:** DB connection lost (if applicable)
- [ ] **Session Loss:** User token expired during chat

#### Test Matrix Table
| Scenario | Input | Expected Output | Status |
|----------|-------|-----------------|--------|
| Valid input | "Ho, sot, dau dau" | Formatted response | ▶️ Test |
| Too short | "Ho" | VALIDATION_ERROR | ▶️ Test |
| Too long | (>500 chars) | VALIDATION_ERROR | ▶️ Test |
| Empty input | "" | VALIDATION_ERROR | ▶️ Test |
| AI timeout | N/A | Retry, then fallback | ▶️ Test |
| Invalid JSON | N/A | Fallback response | ▶️ Test |
| Network error | N/A | Error message | ▶️ Test |
| Concurrent | Multiple users | All get response | ▶️ Test |

#### Implementation
- [ ] Create integration test class: `AIControllerIntegrationTest.java`
- [ ] Create mock AI API server (using WireMock or similar)
- [ ] Implement all scenarios as @Test methods
- [ ] Verify response structures

#### Acceptance Criteria
- [x] All 10+ scenarios tested
- [x] All tests pass
- [x] Edge cases covered

---

### 1.3 Performance Profiling
**Objective:** Identify performance bottlenecks

#### Profiling Areas
- [ ] **AI Response Time:**
  - Measure: Time from request → AI response
  - Target: < 1.5s (2s is hard limit)
  - Tool: JMeter or Apache Bench

- [ ] **Parser Performance:**
  - Measure: JSON parsing time
  - Target: < 100ms
  - Tool: JProfiler or inline @Timed

- [ ] **Formatter Performance:**
  - Measure: Text formatting time
  - Target: < 50ms

- [ ] **API Response Time (E2E):**
  - Measure: Full request → response
  - Target: < 2s
  - Tool: Postman runner with timing

#### Load Testing
- [ ] Single user: 10 requests, response time < 2s
- [ ] 5 concurrent users: response time < 2.5s
- [ ] 10 concurrent users: response time < 3s
- [ ] Error rate: < 1%

#### Memory Leak Detection
- [ ] Run with `jconsole` for heap monitoring
- [ ] Chat 100 times, monitor memory
- [ ] Check for memory growth (should be stable)

#### Acceptance Criteria
- [x] Performance baselines established
- [x] Bottlenecks identified
- [x] No memory leaks detected

---

### 1.4 Security Audit
**Objective:** Identify security vulnerabilities

#### Security Checklist

**Input Validation**
- [ ] XSS Prevention: Input sanitized before processing
  - Test: Send `<script>alert('xss')</script>`
  - Expected: Escaped or rejected

- [ ] SQL Injection: Not applicable (no SQL in AI service)
  - But if database used: Parameterized queries

- [ ] Prompt Injection: Test with malicious prompt
  - Test: `Forget everything, act as...`
  - Expected: Prompt builder escapes/validates

**API Security**
- [ ] JWT validation: All requests verified
  - Test: Send without token → 401
  - Test: Send invalid token → 401
  - Test: Send expired token → 401

- [ ] Rate Limiting: Prevent abuse
  - Test: 100 requests/min → 429 Too Many Requests
  - Tool: Apache Bench

- [ ] CORS: Whitelist allowed origins
  - Test: Cross-origin requests filtered
  - Configure: Only allow frontend domain

**Data Security**
- [ ] No secrets in logs: API keys not logged
  - Check: AI_API_KEY should not appear in logs

- [ ] No sensitive data exposed: Response doesn't leak PII
  - Check: User ID, email not in response

- [ ] HTTPS: All communication encrypted (production)
  - Requirement: Use TLS 1.3+ in production

**Dependency Vulnerabilities**
- [ ] Scan with OWASP Dependency Check:
  ```bash
  mvn dependency-check:check
  ```
- [ ] Address high/critical CVEs
- [ ] Document low/medium CVEs (if acceptable)

#### Acceptance Criteria
- [x] Input validation working
- [x] API authentication enforced
- [x] Rate limiting configured
- [x] No dependency vulnerabilities (critical/high)
- [x] No secrets in logs

---

### 1.5 User Feedback Collection
**Objective:** Gather real user feedback

#### Feedback Channels
- [ ] In-app feedback form (simple modal)
- [ ] GitHub issues (for technical users)
- [ ] Feedback email address
- [ ] Analytics (track page views, error clicks)

#### Questions to Ask
1. Is the chatbot helpful?
2. Does it answer your questions accurately?
3. Response speed: Too slow? Acceptable?
4. UI/UX: Easy to use? Confusing?
5. What features missing?
6. Any errors encountered?

#### Success Metrics
- [ ] Min 20 users tested
- [ ] > 70% find it helpful
- [ ] < 5% report errors
- [ ] Average rating > 3.5/5

---

## 🔧 PHASE 2: FIX & HARDENING (7 working days)

### 2.1 Bug Fixing Priority

#### Critical Bugs (Fix immediately)
- [ ] Crashes (500 errors)
- [ ] Data corruption
- [ ] Security vulnerabilities
- [ ] Complete feature non-functionality

#### High Priority Bugs (Fix within 2 days)
- [ ] AI response accuracy issues
- [ ] Performance > 2s
- [ ] UI layout broken on mobile
- [ ] Important error paths not handled

#### Medium Priority Bugs (Fix within 5 days)
- [ ] Minor UI/UX issues
- [ ] Slow performance (>1.5s but <2s)
- [ ] Incomplete error messages

#### Low Priority Bugs (Schedule for next sprint)
- [ ] Cosmetic issues
- [ ] Nice-to-have features

---

### 2.2 Enhanced Validation

#### Backend Validation Improvements
- [ ] Length constraints: 5-500 chars (already done)
- [ ] Character whitelist: Allow letters, numbers, punctuation only
  ```java
  @Pattern(regexp = "^[\\w\\s\\p{P}]+$")
  String text;
  ```
- [ ] Normalize input: Trim, lowercase for processing
- [ ] Detect spam: Same text repeated 5+ times → throttle

#### Frontend Validation Improvements
- [ ] Real-time validation: Show feedback as user types
- [ ] Debounce: Wait 300ms before validation
- [ ] Show remaining characters: "245 / 500"
- [ ] Disable send if invalid

#### XSS Prevention
- [ ] Escape user input in UI:
  ```jsx
  <div>{escapeHtml(userMessage)}</div>
  ```
- [ ] Never use dangerouslySetInnerHTML
- [ ] Sanitize AI response (even though backend controls it)

---

### 2.3 Error Message Improvements

#### Current Error Messages → Enhanced

| Current | Enhanced |
|---------|----------|
| "VALIDATION_ERROR" | "Vui lòng nhập triệu chứng (tối thiểu 5 ký tự, tối đa 500 ký tự)" |
| "SERVICE_UNAVAILABLE" | "AI đang quá tải, vui lòng thử lại trong vài phút" |
| "TIMEOUT" | "Yêu cầu quá lâu (> 2s). Đôi khi AI chậm, thử lại?" |
| Generic error | "Có lỗi không xác định. Code: ERR_500. Liên hệ: support@healthcare.com" |

#### Error Code Reference
- [ ] Create error code lookup table
- [ ] Each error code maps to user-friendly message
- [ ] Support team can look up error by code

---

### 2.4 Performance Optimization

#### If Response Time > 2s
- [ ] Cache AI responses (with TTL):
  ```
  Key: hash(symptoms)
  Value: cached response
  TTL: 1 hour
  ```

- [ ] Implement timeout fallback:
  ```java
  CompletableFuture<Response> future = callAIAsync();
  Response result = future.get(2, TimeUnit.SECONDS);
  ```

- [ ] Async processing (if needed):
  ```
  1. Accept request
  2. Return "Processing, check back later"
  3. Process in background
  4. Webhook or polling for result
  ```

#### If Memory Usage Growing
- [ ] Profile with JProfiler
- [ ] Check for memory leaks in:
  - String concatenation loops
  - Cached objects not evicted
  - Open HTTP connections
- [ ] Fix identified leaks

---

### 2.5 Monitoring & Alerting

#### Metrics to Monitor
- [ ] Response time (p50, p95, p99)
- [ ] Error rate (%)
- [ ] Request volume (req/min)
- [ ] AI API quota usage
- [ ] Memory usage (heap)
- [ ] CPU usage (%)

#### Alerting Rules
- [ ] Error rate > 5% → Alert
- [ ] Response time p99 > 2s → Alert
- [ ] AI API rate limit → Alert
- [ ] Service down → Immediate alert

#### Implementation
- [ ] Add logging at key points
- [ ] Structured logging: JSON with requestId, duration, errorCode
- [ ] Centralized logging: Send to ELK or Datadog
- [ ] Dashboard: Real-time monitoring

Example log:
```json
{
  "timestamp": "2026-04-29T10:00:00Z",
  "level": "INFO",
  "service": "ai-service",
  "action": "POST /ai/check",
  "requestId": "req-123",
  "userId": "user-456",
  "duration_ms": 1850,
  "status": 200,
  "responseSize": 245,
  "aiApiDuration_ms": 1200,
  "cacheHit": false
}
```

---

### 2.6 Documentation Updates

#### Update Docs
- [ ] SKILL.md: Add AI Service section
- [ ] BA.md: Update with AI scope in MVP
- [ ] TESTING.md: Add AI test cases
- [ ] README.md: Add AI service startup instructions
- [ ] API.md: Document /ai/check endpoint
- [ ] OPERATIONS.md: Add monitoring & troubleshooting

#### API Documentation
```
POST /api/v1/ai/check

Headers:
  Authorization: Bearer <jwt>
  Content-Type: application/json

Body:
{
  "text": "Ho, sot, dau dau"
}

Response (200):
{
  "success": true,
  "data": "🏥 Các bệnh có thể: ...",
  "meta": {
    "requestId": "req-123",
    "timestamp": "2026-04-29T10:00:00Z"
  }
}

Response (400):
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "Symptoms text must be 5-500 characters",
  "meta": { ... }
}

Response (429):
{
  "success": false,
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests",
  "headers": {
    "Retry-After": "60"
  }
}
```

---

## 🚨 PHASE 3: OPERATIONAL RISKS & DEPLOYMENT (7 working days)

### 3.1 Operational Risks Analysis

#### Risk 1: AI API Rate Limiting
**Risk:** Google Gemini API free tier: 15 req/min (may be insufficient)

**Impact:** Users can't get responses during peak usage
- Probability: Medium (depends on user volume)
- Severity: High (core feature blocked)

**Mitigation:**
1. Monitor quota usage (add alerting)
2. Implement request queue (RabbitMQ)
3. Cache responses (1 hour TTL)
4. Upgrade to paid tier if needed
5. Use multiple AI providers (fallback)

**Implementation:**
```java
@Component
public class AIRequestQueue {
    private final Queue<AICheckRequest> queue;
    private final RateLimiter rateLimiter = RateLimiter.create(14); // 14/min (safe margin)
    
    public void enqueue(AICheckRequest request) {
        if (rateLimiter.tryAcquire()) {
            processImmediately(request);
        } else {
            queue.add(request); // Queue for later
        }
    }
}
```

---

#### Risk 2: AI Response Quality
**Risk:** AI may return incorrect medical information

**Impact:** Users get wrong diagnosis, health risks
- Probability: Low-Medium
- Severity: Critical (patient harm)

**Mitigation:**
1. Clear disclaimer (already added)
2. Don't claim diagnosis: "Based on your description, possible conditions could include..."
3. Recommend professional consultation
4. Monitor user feedback for accuracy
5. Implement feedback loop: "Was this helpful?"

**Implementation:**
```java
String disclaimer = "⚠️ LƯU Ý: Đây chỉ là gợi ý dựa trên AI, " +
                   "không thay thế chẩn đoán của bác sĩ. " +
                   "Hãy tham khảo bác sĩ nếu triệu chứng kéo dài.";
```

---

#### Risk 3: Timeout & Performance
**Risk:** AI API slow, response time > 2s

**Impact:** Poor user experience, rate limiting
- Probability: Medium (depends on AI provider)
- Severity: Medium (affects UX)

**Mitigation:**
1. Set timeout: 2s hard limit
2. Retry once on timeout
3. Fallback response on failure
4. Cache responses
5. Use async processing if needed

**Monitoring:**
```
Alert if p99 response time > 2s
Alert if p95 response time > 1.8s
Escalate to DevOps if consistent
```

---

#### Risk 4: Database Performance
**Risk:** Table `chat_history` grows without limit (if implemented in Sprint 3+)

**Impact:** Slow queries, disk space issues
- Probability: Low (if not storing history in MVP)
- Severity: Medium

**Mitigation:**
1. Archival: Move old records to archive table
2. Indexing: Create index on (user_id, created_at)
3. Partitioning: Partition by date (if using PostgreSQL)
4. Data retention: Delete records > 90 days old

```sql
-- Create index
CREATE INDEX idx_chat_history_user_date ON chat_history(user_id, created_at);

-- Archive old records
INSERT INTO chat_history_archive
SELECT * FROM chat_history WHERE created_at < NOW() - INTERVAL '90 days';

DELETE FROM chat_history WHERE created_at < NOW() - INTERVAL '90 days';
```

---

#### Risk 5: Security: Prompt Injection
**Risk:** User input manipulates AI prompt

**Impact:** AI behaves unexpectedly, leaks information
- Probability: Low (users don't know about prompt structure)
- Severity: High

**Mitigation:**
1. Escape user input
2. Whitelist allowed characters
3. Validate prompt before sending
4. Use separate prompt template (never interpolate user input directly)

**Implementation:**
```java
public String buildSafePrompt(String userInput) {
    // Don't: "Analyze: " + userInput + " Return..."
    // Do: Use parameterized prompt
    
    String template = "Analyze the following symptoms: %s\n\nReturn...";
    String sanitized = sanitize(userInput); // Remove special chars
    return String.format(template, sanitized);
}
```

---

#### Risk 6: Availability & Fallback
**Risk:** AI service crashes, frontend hangs

**Impact:** Users can't use chat
- Probability: Low (Spring Boot stable)
- Severity: Medium

**Mitigation:**
1. Health checks: /actuator/health
2. Graceful degradation: Return fallback response
3. Circuit breaker: If AI fails N times, fail fast
4. Blue-green deployment: Zero-downtime updates

**Circuit Breaker Pattern:**
```java
@Component
public class AICircuitBreaker {
    private int failureCount = 0;
    private static final int THRESHOLD = 5;
    
    public boolean isOpen() {
        return failureCount >= THRESHOLD;
    }
    
    public void recordSuccess() { failureCount = 0; }
    public void recordFailure() { failureCount++; }
}
```

---

### 3.2 Deployment Risk Analysis

#### Pre-Production Checklist
- [ ] Code review: All PRs approved
- [ ] Automated tests: 100% pass
- [ ] Manual testing: Smoke test passed
- [ ] Security scan: No critical vulnerabilities
- [ ] Performance test: Response time < 2s
- [ ] Staging deployment: Verify in staging env
- [ ] Rollback plan: Documented & tested
- [ ] Monitoring: Alerts configured

---

#### Deployment Steps
1. **Pre-deployment:**
   ```bash
   # Run all checks
   mvn clean verify
   npm run build
   npm run test
   ```

2. **Database migration:**
   ```bash
   # Flyway runs automatically on startup
   # Or manual: mvn flyway:migrate -Dflyway.url=...
   ```

3. **Backend deployment:**
   ```bash
   # Build Docker image
   docker build -t ai-service:v1.0.0 .
   
   # Push to registry
   docker push registry.com/ai-service:v1.0.0
   
   # Deploy to prod
   kubectl set image deployment/ai-service \
     ai-service=registry.com/ai-service:v1.0.0
   ```

4. **Frontend deployment:**
   ```bash
   # Build & push
   npm run build
   docker build -t frontend:v1.0.0 .
   docker push registry.com/frontend:v1.0.0
   
   # Deploy
   kubectl set image deployment/frontend \
     frontend=registry.com/frontend:v1.0.0
   ```

5. **Post-deployment:**
   ```bash
   # Health check
   curl https://api.healthcare.com/actuator/health
   
   # Smoke test
   curl https://api.healthcare.com/ai/check -X POST \
     -H "Content-Type: application/json" \
     -d '{"text": "Ho, sot"}'
   ```

---

#### Rollback Plan
```bash
# If deployment fails:
kubectl rollout undo deployment/ai-service
kubectl rollout undo deployment/frontend

# Verify rollback
kubectl rollout status deployment/ai-service
```

---

### 3.3 Disaster Recovery Plan

#### Scenario 1: AI API Down (Google Gemini unavailable)
**Recovery:**
1. Activate fallback response (already implemented)
2. Switch to alternative AI provider (if available)
3. Notify users: "AI temporarily unavailable"
4. Contact Google support

**Recovery Time:** < 1 minute (fallback)

---

#### Scenario 2: Database Down
**Recovery:**
1. Fail over to replica (if applicable)
2. Restore from backup
3. Re-run Flyway migrations

**Recovery Time:** 5-15 minutes

**Prevention:**
- Daily backups
- Test restore monthly
- Master-slave replication

---

#### Scenario 3: Service Out of Memory
**Recovery:**
1. Restart service (Kubernetes auto-restarts)
2. Increase heap size
3. Fix memory leak

**Prevention:**
- Monitor memory usage
- Implement memory limits in Kubernetes

---

#### Scenario 4: High Traffic / DDoS
**Recovery:**
1. Enable rate limiting (already planned)
2. Scale horizontally: Add more instances
3. Use CDN/WAF to filter traffic
4. Contact hosting provider

**Prevention:**
- Setup CDN (CloudFlare, AWS CloudFront)
- WAF rules to block suspicious traffic
- Rate limiting per IP
- Monitor traffic patterns

---

### 3.4 Scaling Strategy

#### Horizontal Scaling
```yaml
# Kubernetes HPA (Horizontal Pod Autoscaler)
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ai-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ai-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

---

#### Caching Strategy
```
Layer 1: Redis (in-memory cache)
  Key: hash(user_input)
  TTL: 1 hour
  
Layer 2: CDN (edge cache) - if applicable
  TTL: 1 hour
  
Layer 3: DB (persistent storage) - if storing history
  Index on (user_id, created_at)
```

---

### 3.5 Monitoring & Observability

#### Key Metrics
```
1. Request Rate: requests/sec
   Alert if: > 100 req/sec (adjust based on capacity)

2. Response Time: p50, p95, p99
   Alert if: p99 > 2s

3. Error Rate: errors/sec
   Alert if: > 5%

4. AI API Quota: remaining requests
   Alert if: < 50 requests remaining

5. CPU Usage: %
   Alert if: > 80%

6. Memory Usage: heap %
   Alert if: > 85%

7. Database Connections: active
   Alert if: > 80 of pool size

8. Cache Hit Rate: %
   Alert if: < 50% (may indicate cache issues)
```

#### Logging
```json
{
  "timestamp": "2026-04-29T10:00:00Z",
  "level": "INFO",
  "logger": "com.healthcare.ai.controller.AIController",
  "message": "POST /api/v1/ai/check",
  "requestId": "req-e4d9-4a6c-b2f1",
  "userId": "user-123",
  "duration_ms": 1850,
  "httpStatus": 200,
  "aiProvider": "gemini",
  "cacheHit": false,
  "errorCode": null
}
```

---

### 3.6 Compliance & Data Privacy

#### GDPR Compliance (if applicable)
- [ ] Data retention policy: Delete chat history after 90 days
- [ ] User consent: Show disclaimer before first use
- [ ] Data portability: Ability to export chat history
- [ ] Right to be forgotten: Implement data deletion

#### HIPAA Compliance (if handling patient data)
- [ ] Encryption at rest: AES-256
- [ ] Encryption in transit: TLS 1.3+
- [ ] Access controls: RBAC enforced
- [ ] Audit logs: All access logged
- [ ] Business Associate Agreement: Signed with providers

#### Data Classification
```
Public: AI response text (non-personalized)
Confidential: User symptoms, chat history
Restricted: User ID, medical history (if stored)

Storage:
Public → CDN, public cache
Confidential → Encrypted DB, Redis with TTL
Restricted → Encrypted DB, audit logged
```

---

## 📊 POST-TASK3 TIMELINE

| Week | Phase | Deliverables |
|------|-------|-------------|
| **W1** | Error Analysis | Coverage report, test matrix, security audit |
| **W1-W2** | Fixes & Hardening | Bug fixes, enhanced validation, performance optimization |
| **W2** | Monitoring Setup | Alerts configured, dashboards created |
| **W2-W3** | Risk & Deployment | Disaster recovery plan, scaling strategy documented |
| **W3** | Production Ready | Deployment checklist complete, ready to go live |

---

## ✅ FINAL CHECKLIST

### Code Quality
- [ ] Test coverage > 80%
- [ ] All tests pass
- [ ] No critical bugs
- [ ] Code reviewed
- [ ] Documentation updated

### Performance
- [ ] Response time < 2s
- [ ] No memory leaks
- [ ] CPU usage < 80%
- [ ] Database optimized

### Security
- [ ] Input validation enforced
- [ ] XSS prevention implemented
- [ ] No dependency vulnerabilities
- [ ] CORS configured
- [ ] Rate limiting active

### Operations
- [ ] Monitoring & alerting configured
- [ ] Rollback plan documented
- [ ] Disaster recovery tested
- [ ] Scaling strategy defined
- [ ] On-call playbook created

### Deployment
- [ ] Pre-deployment checklist passed
- [ ] Staging deployment successful
- [ ] Smoke tests passed
- [ ] Team trained
- [ ] Go-live date confirmed

---

## 🎯 SUCCESS CRITERIA

- ✅ Zero critical bugs in production
- ✅ Response time < 2s (95% of requests)
- ✅ Error rate < 1%
- ✅ Uptime > 99.5%
- ✅ User satisfaction > 4/5
- ✅ No security incidents

---

**Document Created:** 2026-04-29  
**Last Updated:** 2026-04-29  
**Status:** Ready for Post-Task3 Phase

Next: Begin implementation after Task3 completion!
