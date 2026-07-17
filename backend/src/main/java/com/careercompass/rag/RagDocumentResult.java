package com.careercompass.rag;

public record RagDocumentResult(
        Long id,
        String title,
        String content,
        String documentType,
        String onetCode,
        double score
) {
}
