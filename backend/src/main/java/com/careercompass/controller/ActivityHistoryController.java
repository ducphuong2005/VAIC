package com.careercompass.controller;

import com.careercompass.dto.response.ActivityLogResponse;
import com.careercompass.dto.response.ApiResponse;
import com.careercompass.security.UserPrincipal;
import com.careercompass.service.ActivityLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity-history")
@RequiredArgsConstructor
public class ActivityHistoryController {
    private final ActivityLogService service;

    @GetMapping
    public ApiResponse<List<ActivityLogResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(service.list(principal.getId()));
    }
}
