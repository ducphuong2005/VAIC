# AI Architecture

Career Compass không để LLM tự chọn nghề từ đầu.

## Recommendation

Recommendation Engine tính bằng Java:

```text
finalScore =
interestScore * 0.25
+ abilityScore * 0.25
+ skillScore * 0.15
+ workStyleScore * 0.10
+ marketScore * 0.15
+ feasibilityScore * 0.10
```

Weights được cấu hình qua `app.recommendation.weights`.

## Ethical AI

Không dùng:

- gender
- ethnicity
- hometown

Region chỉ dùng cho:

- market score
- job availability
- local learning options

Guardrail wording tránh khẳng định tuyệt đối và luôn đưa nhiều lựa chọn.

## LLM

Interface:

```java
public interface LlmProvider {
    LlmResponse generate(LlmRequest request);
}
```

Providers:

- `MockLlmProvider`
- `GeminiLlmProvider`
- `OpenAiLlmProvider`

Local mặc định dùng `LLM_PROVIDER=gemini`; có thể đổi lại OpenAI bằng `LLM_PROVIDER=openai`. Mọi call được lưu vào `llm_calls`.

## RAG

MVP dùng MySQL keyword filtering:

- `rag_documents`
- `rag_retrieval_logs`

Chatbot chỉ lấy top documents, không đưa toàn bộ database vào prompt.

## Chat Flow

1. Save user message.
2. Build profile/career/job/RAG context.
3. LLM 1 đọc context và sinh intent, RAG query, response guidance.
4. Retrieve RAG docs theo định hướng từ LLM 1.
5. LLM 2 nhận guidance + context và sinh câu trả lời JSON cuối.
6. Validate JSON, không dùng câu trả lời fix cứng khi LLM lỗi.
7. Apply guardrail.
8. Save assistant response.
