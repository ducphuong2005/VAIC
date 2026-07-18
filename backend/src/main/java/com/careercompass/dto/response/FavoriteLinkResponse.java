package com.careercompass.dto.response;

import java.time.Instant;

public record FavoriteLinkResponse(
        Long id,
        String linkType,
        String provider,
        String title,
        String url,
        String description,
        Instant createdAt
) {
}
