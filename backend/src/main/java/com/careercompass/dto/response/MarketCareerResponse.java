package com.careercompass.dto.response;

import java.util.List;

public record MarketCareerResponse(
        String onetCode,
        List<MarketSignalResponse> signals,
        String limitation
) {
}
