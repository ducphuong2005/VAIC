package com.careercompass.dto.response;

import java.util.List;

public record CareerDetailResponse(
        String onetCode,
        String titleVi,
        String titleEn,
        String description,
        String careerCluster,
        Integer jobZone,
        List<CareerElementResponse> topSkills,
        List<CareerElementResponse> topAbilities,
        List<CareerElementResponse> topInterests,
        List<CareerElementResponse> topWorkStyles,
        List<TechnologySkillResponse> technologySkills,
        List<CareerTaskResponse> tasks,
        List<RelatedCareerResponse> relatedOccupations,
        List<MarketSignalResponse> marketSignals
) {
}
