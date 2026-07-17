package com.careercompass.controller;

import com.careercompass.dto.request.ProfileUpdateRequest;
import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.ProfileCompletionResponse;
import com.careercompass.dto.response.ProfileDimensionResponse;
import com.careercompass.dto.response.ProfileEvidenceResponse;
import com.careercompass.dto.response.ProfileResponse;
import com.careercompass.security.UserPrincipal;
import com.careercompass.service.ProfileService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ApiResponse<ProfileResponse> profile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(profileService.getProfile(principal.getId()));
    }

    @PutMapping
    public ApiResponse<ProfileResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return ApiResponse.success(profileService.updateProfile(principal.getId(), request));
    }

    @GetMapping("/dimensions")
    public ApiResponse<List<ProfileDimensionResponse>> dimensions(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(profileService.dimensions(principal.getId()));
    }

    @GetMapping("/evidence")
    public ApiResponse<List<ProfileEvidenceResponse>> evidence(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(profileService.evidence(principal.getId()));
    }

    @PostMapping("/recalculate")
    public ApiResponse<List<ProfileDimensionResponse>> recalculate(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(profileService.recalculate(principal.getId()));
    }

    @GetMapping("/completion")
    public ApiResponse<ProfileCompletionResponse> completion(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(profileService.completion(principal.getId()));
    }
}
