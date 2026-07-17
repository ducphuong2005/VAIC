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
- `LLM_PROVIDER`, `LLM_API_KEY`

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

## Docker

```powershell
.\mvnw.cmd package -DskipTests
docker compose up --build
```

Services:

- `mysql`
- `spring-backend`
- `python-crawler` placeholder
- `redis` optional profile

## Crawler Integration

Crawler Python không chạy trong request Spring Boot. Crawler gửi dữ liệu qua:

- `POST /api/internal/v1/crawler/runs`
- `POST /api/internal/v1/jobs/batch`
- `POST /api/internal/v1/market-signals/recalculate`

Header:

```text
X-Internal-Api-Key: ${INTERNAL_CRAWLER_API_KEY}
```

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
- LLM default là `mock`; bật provider thật bằng env var.
- TopCV market signals là sample data, không đại diện toàn bộ thị trường Việt Nam.
