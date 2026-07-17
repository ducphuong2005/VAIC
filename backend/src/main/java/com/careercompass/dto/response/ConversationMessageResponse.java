package com.careercompass.dto.response;

import java.time.Instant;
import java.util.List;

public record ConversationMessageResponse(
        Long id,
        String sender,
        String content,
        String intent,
        Integer confidence,
        List<String> sources,
        Instant createdAt
) {
}
