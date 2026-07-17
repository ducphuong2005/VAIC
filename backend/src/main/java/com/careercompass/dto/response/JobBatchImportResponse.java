package com.careercompass.dto.response;

public record JobBatchImportResponse(
        int inserted,
        int updated,
        int duplicate,
        int rejected
) {
}
