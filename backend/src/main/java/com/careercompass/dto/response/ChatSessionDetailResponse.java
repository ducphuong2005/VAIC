package com.careercompass.dto.response;

import java.util.List;
import java.util.UUID;

public record ChatSessionDetailResponse(
        UUID id,
        String title,
        List<ConversationMessageResponse> messages
) {
}
