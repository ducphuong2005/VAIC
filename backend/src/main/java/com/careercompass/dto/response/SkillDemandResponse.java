package com.careercompass.dto.response;

public record SkillDemandResponse(
        String skillName,
        long postingCount,
        String source
) {
}
