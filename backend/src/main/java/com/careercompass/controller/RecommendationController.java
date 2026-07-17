package com.careercompass.controller;

import com.careercompass.dto.request.GenerateRecommendationRequest;
import com.careercompass.dto.request.RecommendationFeedbackRequest;
import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.RecommendationDetailResponse;
import com.careercompass.dto.response.RecommendationFeedbackResponse;
import com.careercompass.dto.response.RecommendationRunResponse;
import com.careercompass.dto.response.SkillGapResponse;
import com.careercompass.security.UserPrincipal;
import com.careercompass.service.RecommendationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/generate")
    public ApiResponse<RecommendationRunResponse> generate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GenerateRecommendationRequest request
    ) {
        return ApiResponse.success(recommendationService.generate(principal.getId(), request));
    }

    @GetMapping("/latest")
    public ApiResponse<RecommendationRunResponse> latest(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(recommendationService.latest(principal.getId()));
    }

    @GetMapping("/runs/{runId}")
    public ApiResponse<RecommendationRunResponse> run(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID runId
    ) {
        return ApiResponse.success(recommendationService.run(principal.getId(), runId));
    }

    @GetMapping("/{id}")
    public ApiResponse<RecommendationDetailResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(recommendationService.detail(principal.getId(), id));
    }

    @PostMapping("/{id}/feedback")
    public ApiResponse<RecommendationFeedbackResponse> feedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RecommendationFeedbackRequest request
    ) {
        return ApiResponse.success(recommendationService.feedback(principal.getId(), id, request));
    }

    @GetMapping("/{id}/skill-gaps")
    public ApiResponse<List<SkillGapResponse>> skillGaps(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(recommendationService.skillGaps(principal.getId(), id));
    }
}
