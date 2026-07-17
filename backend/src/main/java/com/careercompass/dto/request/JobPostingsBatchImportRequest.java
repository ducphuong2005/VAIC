package com.careercompass.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record JobPostingsBatchImportRequest(
        Long crawlRunId,
        @NotEmpty List<@Valid JobPostingImportRequest> jobs
) {
}
