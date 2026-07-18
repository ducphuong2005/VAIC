# Career Compass Backend

Spring Boot backend cho Career Compass: hướng nghiệp AI dựa trên assessment, mini-game evidence, O*NET career data, TopCV market samples, recommendation engine và chatbot RAG.

## Kiến trúc

- Java 21, Spring Boot 3.3, Maven.
- MySQL 8, Spring Data JPA, Flyway migrations.
- Spring Security JWT + internal crawler API key.
- DTO-only API responses, không trả entity trực tiếp.
- Layering: Controller → Service → Repository → Database.
- Recommendation engine tính bằng code; LLM chỉ giải thích/chat, không tự quyết định nghề.

## Chạy local

```powershell
.\mvnw.cmd test
.\mvnw.cmd package -DskipTests
java -jar target\career-compass-backend-0.0.1-SNAPSHOT.jar
```

Khởi động nhanh không cần MySQL:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Nếu chạy với MySQL local, ứng dụng mặc định dùng `DB_NAME=career_compass`, `DB_USERNAME=career_compass`, `DB_PASSWORD=career_compass`. Lỗi `Access denied for user 'career_compass'@'localhost'` nghĩa là MySQL local không có user này hoặc password không khớp.

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Environment Variables

Xem `application-example.yml`.

Biến bắt buộc production:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `INTERNAL_CRAWLER_API_KEY`
- `LLM_PROVIDER`, `GEMINI_API_KEY` hoặc `LLM_API_KEY`

## Database Migration

Flyway chạy tự động khi app start.

- `V1` users/security baseline.
- `V2` O*NET career tables + seed demo.
- `V3` student profile + dynamic profile.
- `V4` assessment.
- `V5` mini-game.
- `V6` labor market/crawler.
- `V7` recommendation/skill gap.
- `V8` chat/LLM/RAG.
- `V9` learning/favorites/history/fairness.
- `V10` mở rộng đủ 6 nhóm RIASEC + RAG context cho AI advice.

## Docker

```powershell
.\mvnw.cmd package -DskipTests
docker compose up --build
```

Services:

- `mysql`
- `spring-backend`
- `python-crawler` chạy crawler mẫu TopCV và gửi job vào backend
- `redis` optional profile

## LLM / Gemini / ChatGPT

Mặc định local dùng `LLM_PROVIDER=gemini`; test profile mới dùng `mock`. Chatbot không fallback sang câu trả lời fix cứng khi thiếu key hoặc lỗi LLM.

Chạy với Gemini API thật:

```powershell
$env:LLM_PROVIDER="gemini"
$env:GEMINI_API_KEY="AIza..."
$env:LLM_MODEL="gemini-2.0-flash"
$env:LLM_CONTEXT_MODEL="gemini-2.0-flash"
$env:LLM_ANSWER_MODEL="gemini-2.0-flash"
.\mvnw.cmd spring-boot:run
```

Chạy với ChatGPT/OpenAI API thật:

```powershell
$env:LLM_PROVIDER="openai"
$env:LLM_API_KEY="sk-..."
$env:LLM_MODEL="gpt-4o-mini"
$env:LLM_CONTEXT_MODEL="gpt-4o-mini"
$env:LLM_ANSWER_MODEL="gpt-4o-mini"
.\mvnw.cmd spring-boot:run
```

Chatbot dùng pipeline 2 lượt LLM:

- LLM 1 (`OPEN_RESPONSE_ANALYSIS`): đọc tin nhắn, lịch sử hội thoại, RIASEC/career/job context và định hướng intent, RAG query, cách trả lời.
- LLM 2 (`CHAT_RESPONSE`): nhận định hướng từ LLM 1 cùng RAG/job context để tạo câu trả lời cuối.

`GeminiLlmProvider` gọi Gemini `generateContent`, gửi `x-goog-api-key`, yêu cầu JSON schema response, parse `candidates[0].content.parts[0].text`, và lưu từng lượt gọi vào `llm_calls`. `OpenAiLlmProvider` vẫn có sẵn cho `LLM_PROVIDER=openai`. Nếu thiếu key hoặc LLM trả JSON lỗi, API trả lỗi rõ ràng thay vì tự sinh câu mẫu.

## Crawler Integration

Crawler Python không chạy trong request Spring Boot. Crawler gửi dữ liệu qua:

- `POST /api/internal/v1/crawler/runs`
- `POST /api/internal/v1/jobs/batch`
- `POST /api/internal/v1/market-signals/recalculate`

Header:

```text
X-Internal-Api-Key: ${INTERNAL_CRAWLER_API_KEY}
```

Crawler mẫu TopCV:

```powershell
$env:BACKEND_URL="http://localhost:8080"
$env:INTERNAL_CRAWLER_API_KEY="dev-crawler-api-key-change-me"
python .\crawler\topcv_sample_crawler.py
```

Script cố gắng đọc trang TopCV, map keyword sang O*NET demo, import jobs, rồi gọi recalculate market signals. Nếu không parse được listing, script dùng vài job mẫu để pipeline vẫn chạy.

## AI Advice Flow

Sau khi hoàn thành assessment hoặc mini-game, response có thêm `advice`:

- `content`: lời khuyên AI bằng tiếng Việt.
- `topRiasecTypes`: các nhóm RIASEC nổi bật.
- `careerOptions`: nghề gợi ý do recommendation engine chấm điểm.
- `marketJobs`: job đã crawl/import từ TopCV trong database.
- `nextSteps`: bước hành động tiếp theo.
- `sources`: nguồn RAG/job context đã dùng.

## Frontend Integration

Mọi response dùng envelope:

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2026-07-17T10:00:00Z"
}
```

Fetch example:

```js
const res = await fetch("http://localhost:8080/api/v1/careers/search?q=Data", {
  headers: { Authorization: `Bearer ${accessToken}` }
});
const body = await res.json();
console.log(body.data);
```

## Testing

```powershell
.\mvnw.cmd test
```

Test profile:

- JWT auth
- Profile aggregation
- Assessment scoring
- Mini-game normalization
- Career API
- Crawler import/dedup
- Recommendation formula + flow
- Chat flow
- Learning/favorites/history/fairness

## Limitations

- O*NET/TopCV data hiện là seed demo/MVP.
- RAG MVP dùng keyword search trong MySQL, chưa dùng vector DB.
- Test profile dùng `LLM_PROVIDER=mock`; local mặc định dùng `gemini`.
- TopCV market signals là sample data, không đại diện toàn bộ thị trường Việt Nam.
