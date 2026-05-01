Task: AI-Service Bug Fixes & Hardening
Giai đoạn 1 — Sửa ngay
 BUG-03: Thêm logging vào AIClient
 BUG-01: Tăng timeout lên 5s + set API key
 BUG-02: Thêm Content-Type header
 BUG-04: Fix N+1 query trong activate()
Giai đoạn 2 — Hardening
 BUG-05/R3: Thêm rate limiting
 BUG-07/R4: Validate JWT secret on startup
 BUG-08: Restrict preview endpoint cho ADMIN
 R1: Cải thiện prompt injection protection
Giai đoạn 3 — Production readiness
 R12: Config CORS từ environment
 R13: Custom health indicator cho Gemini API
 Update run-service.ps1 cho đầy đủ services + AI_API_KEY