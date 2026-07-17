package com.careercompass.controller;

import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.FairnessTestRunResponse;
import com.careercompass.service.FairnessService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/fairness-tests")
@RequiredArgsConstructor
public class AdminFairnessController {
    private final FairnessService service;

    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FairnessTestRunResponse> run() {
        return ApiResponse.success(service.run());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FairnessTestRunResponse>> list() {
        return ApiResponse.success(service.list());
    }
}
