package com.careercompass.rag;

import com.careercompass.entity.DocumentType;

public record RagQuery(
        String query,
        String onetCode,
        String language,
        String region,
        DocumentType documentType,
        String sourceVersion,
        int limit
) {
}
