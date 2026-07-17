package com.careercompass.controller;

import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.MarketCareerResponse;
import com.careercompass.dto.response.MarketSignalResponse;
import com.careercompass.dto.response.SkillDemandResponse;
import com.careercompass.dto.response.TrendingCareerResponse;
import com.careercompass.service.MarketSignalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketSignalService marketSignalService;

    @GetMapping("/trending")
    public ApiResponse<List<TrendingCareerResponse>> trending(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(marketSignalService.trending(Math.min(limit, 50)));
    }

    @GetMapping("/skills-in-demand")
    public ApiResponse<List<SkillDemandResponse>> skillsInDemand(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(marketSignalService.skillsInDemand(Math.min(limit, 100)));
    }

    @GetMapping("/regions")
    public ApiResponse<List<String>> regions() {
        return ApiResponse.success(marketSignalService.regions());
    }

    @GetMapping("/careers/{onetCode}")
    public ApiResponse<MarketCareerResponse> careerMarket(@PathVariable String onetCode) {
        return ApiResponse.success(marketSignalService.careerMarket(onetCode));
    }

    @GetMapping("/careers/{onetCode}/trend")
    public ApiResponse<List<MarketSignalResponse>> careerTrend(
            @PathVariable String onetCode,
            @RequestParam(required = false) String region
    ) {
        return ApiResponse.success(marketSignalService.careerTrend(onetCode, region));
    }
}
