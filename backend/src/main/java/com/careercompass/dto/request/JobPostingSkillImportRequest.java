package com.careercompass.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record JobPostingSkillImportRequest(
        @NotEmpty List<@NotBlank @Size(max = 190) String> skills
) {
}
