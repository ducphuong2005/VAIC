package com.careercompass.controller;

import com.careercompass.dto.request.SubmitAssessmentAnswersRequest;
import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.AssessmentDetailResponse;
import com.careercompass.dto.response.AssessmentResultResponse;
import com.careercompass.dto.response.AssessmentSessionResponse;
import com.careercompass.dto.response.AssessmentSummaryResponse;
import com.careercompass.security.UserPrincipal;
import com.careercompass.service.AssessmentService;
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
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/api/v1/assessments")
    public ApiResponse<List<AssessmentSummaryResponse>> assessments() {
        return ApiResponse.success(assessmentService.list());
    }

    @GetMapping("/api/v1/assessments/{id}")
    public ApiResponse<AssessmentDetailResponse> detail(@PathVariable UUID id) {
        return ApiResponse.success(assessmentService.detail(id));
    }

    @PostMapping("/api/v1/assessments/{id}/start")
    public ApiResponse<AssessmentSessionResponse> start(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return ApiResponse.success(assessmentService.start(principal.getId(), id));
    }

    @PostMapping("/api/v1/assessment-sessions/{sessionId}/answers")
    public ApiResponse<AssessmentSessionResponse> answers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SubmitAssessmentAnswersRequest request
    ) {
        return ApiResponse.success(assessmentService.submitAnswers(principal.getId(), sessionId, request));
    }

    @PostMapping("/api/v1/assessment-sessions/{sessionId}/complete")
    public ApiResponse<AssessmentResultResponse> complete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ApiResponse.success(assessmentService.complete(principal.getId(), sessionId));
    }

    @GetMapping("/api/v1/assessment-sessions/{sessionId}/result")
    public ApiResponse<AssessmentResultResponse> result(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ApiResponse.success(assessmentService.result(principal.getId(), sessionId));
    }
}
