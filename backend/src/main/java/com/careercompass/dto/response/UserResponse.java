package com.careercompass.dto.response;

import com.careercompass.entity.UserRole;
import com.careercompass.entity.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        UserRole role,
        UserStatus status,
        Instant createdAt
) {
}
