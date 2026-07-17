package com.careercompass.controller;

import com.careercompass.dto.request.GenerateLearningPathRequest;
import com.careercompass.dto.request.UpdateStepProgressRequest;
import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.LearningPathResponse;
import com.careercompass.security.UserPrincipal;
import com.careercompass.service.LearningPathService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {
    private final LearningPathService service;

    @PostMapping("/generate")
    public ApiResponse<LearningPathResponse> generate(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody GenerateLearningPathRequest request) {
        return ApiResponse.success(service.generate(principal.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<LearningPathResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(service.list(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<LearningPathResponse> get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return ApiResponse.success(service.get(principal.getId(), id));
    }

    @PutMapping("/{id}/steps/{stepId}/progress")
    public ApiResponse<LearningPathResponse> progress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @PathVariable Long stepId,
            @Valid @RequestBody UpdateStepProgressRequest request
    ) {
        return ApiResponse.success(service.updateStep(principal.getId(), id, stepId, request));
    }
}
