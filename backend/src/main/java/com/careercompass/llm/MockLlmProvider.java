package com.careercompass.llm;

import org.springframework.stereotype.Component;

@Component
public class MockLlmProvider implements LlmProvider {

    @Override
    public String providerName() {
        return "mock";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        String content = """
                {"content":"Dựa trên dữ liệu hiện tại, đây là một trong các hướng bạn có thể khám phá. Độ tin cậy hiện tại ở mức trung bình-khá; hãy tiếp tục bổ sung evidence từ assessment và mini-game.","confidence":75}
                """;
        return new LlmResponse(content, 50, 40, true, null);
    }
}
