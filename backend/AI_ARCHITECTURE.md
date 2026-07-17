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
- `OpenAiLlmProvider`
- `GeminiLlmProvider`

Mọi call được lưu vào `llm_calls`.

## RAG

MVP dùng MySQL keyword filtering:

- `rag_documents`
- `rag_retrieval_logs`

Chatbot chỉ lấy top documents, không đưa toàn bộ database vào prompt.

## Chat Flow

1. Save user message.
2. Detect intent.
3. Retrieve RAG docs.
4. Build prompt.
5. Call LLM provider.
6. Validate JSON/fallback.
7. Apply guardrail.
8. Save assistant response.
