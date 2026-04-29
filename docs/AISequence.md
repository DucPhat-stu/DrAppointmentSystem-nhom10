# AI FLOW - SEQUENCE DIAGRAM (PRODUCTION READY)

---

1. HIGH-LEVEL FLOW (END-TO-END)

---

Actors:

* User
* Frontend (Chat/Form UI)
* API Gateway
* AI Service
* Prompt Service
* Prompt DB
* External AI API (Gemini/OpenAI)
* Doctor Service (optional)

Flow:

1. User nhập triệu chứng (text hoặc form)

2. Frontend gửi request:
   POST /ai/check

3. API Gateway forward request đến AI Service (kèm JWT)

4. AI Service gọi Prompt Service:
   → Lấy prompt template active

5. Prompt Service query DB:
   SELECT * FROM prompt_templates WHERE is_active = true

6. DB trả về template

7. AI Service build prompt:

   * Replace variables (symptoms, duration, description)
   * Append instruction cho AI

8. AI Service gọi External AI API:

   * Gửi prompt
   * Timeout < 2s

9. External AI trả response:

   * JSON hoặc text

10. AI Service xử lý:

    * Parse JSON
    * Validate data
    * Fallback nếu lỗi

11. AI Service format dữ liệu:
    → chuyển thành text dễ đọc

12. (Optional) AI Service gọi Doctor Service:
    → map chuyên khoa → danh sách bác sĩ

13. AI Service trả response về API Gateway

14. API Gateway trả về Frontend

15. Frontend hiển thị kết quả chatbot cho User

---

2. LOW-LEVEL BACKEND FLOW (CHI TIẾT LOGIC)

---

Components:

* Controller
* Service
* PromptBuilder
* PromptRepository
* AIClient
* Parser
* Formatter

Flow:

1. FE → API:
   POST /ai/check (JSON symptoms)

2. API → Controller

3. Controller → Service:
   handleRequest()

4. Service → PromptRepository:
   getActiveTemplate()

5. Repository → DB:
   query template

6. DB → Repository → Service:
   return template

7. Service → PromptBuilder:
   buildPrompt(data, template)

8. PromptBuilder:

   * replace {{symptoms}}
   * replace {{duration}}
   * replace {{description}}

9. PromptBuilder → Service:
   return finalPrompt

10. Service → AIClient:
    callAI(finalPrompt)

11. AIClient:

    * set timeout < 2s
    * send HTTP request

12. AI API → AIClient:
    return rawResponse

13. Service → Parser:
    parseJSON(rawResponse)

14. Parser:

    * extract JSON
    * validate fields

15. Parser → Service:
    return structuredData

16. Service → Formatter:
    toText(structuredData)

17. Formatter:

    * build readable message
    * add disclaimer

18. Formatter → Service:
    return finalText

19. Service → Controller:
    return response DTO

20. Controller → API → FE:
    return result

---

3. ERROR HANDLING FLOW

---

Scenario 1: AI Timeout / API Failure

1. AI Service → AIClient:
   call AI

2. Nếu timeout hoặc fail:

   * retry 1 lần

3. Nếu vẫn fail:

   * return fallback response:
     "Không thể xử lý yêu cầu, vui lòng thử lại"

---

Scenario 2: AI trả sai JSON

1. AI Service → Parser:
   parse JSON

2. Nếu JSON invalid:

   * log error
   * fallback response:
     {
     possible_conditions: [],
     symptoms_detected: [],
     recommended_specialty: "Không xác định",
     advice: "Vui lòng mô tả rõ hơn"
     }

3. System vẫn trả response (KHÔNG crash)

---

4. TECHNICAL CONDITIONS (BẮT BUỘC)

---

* Timeout AI: <= 2 giây
* Retry: tối đa 1 lần
* Không cho FE gọi trực tiếp AI
* Prompt chỉ build ở Backend
* Luôn parse JSON trước khi dùng
* Luôn convert JSON → text
* Chỉ có 1 prompt template active

---

5. FORBIDDEN (KHÔNG ĐƯỢC LÀM)

---

* Hardcode prompt ở nhiều nơi
* Trả raw AI response về FE
* Bỏ qua bước Parser
* FE tự build prompt
* Không validate input

---

6. OUTPUT FORMAT (CHUẨN CUỐI)

---

Ví dụ output:

Có thể bạn đang gặp: Cảm cúm
Triệu chứng ghi nhận: ho, sốt
Gợi ý khám: Nội tổng quát
Lời khuyên: Nghỉ ngơi, uống nước

(Lưu ý: Đây chỉ là gợi ý từ AI, không thay thế chẩn đoán y khoa)

---

## END OF FILE
