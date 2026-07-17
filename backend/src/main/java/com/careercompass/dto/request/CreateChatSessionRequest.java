package com.careercompass.dto.request;

import jakarta.validation.constraints.Size;

public record CreateChatSessionRequest(@Size(max = 255) String title) {
}
