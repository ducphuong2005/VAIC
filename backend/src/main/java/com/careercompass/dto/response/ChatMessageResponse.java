package com.careercompass.dto.response;

import java.util.List;

public record ChatMessageResponse(
        Long messageId,
        String content,
        String intent,
        List<Object> profileUpdates,
        List<Object> recommendations,
        List<String> sources,
        int confidence
) {
}
