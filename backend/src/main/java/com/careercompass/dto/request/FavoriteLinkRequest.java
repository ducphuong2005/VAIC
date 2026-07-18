package com.careercompass.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FavoriteLinkRequest(
        @NotBlank @Size(max = 40) String linkType,
        @NotBlank @Size(max = 120) String provider,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 700) String url,
        @Size(max = 1000) String description
) {
}
