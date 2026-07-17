package com.careercompass.controller;

import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.CareerSummaryResponse;
import com.careercompass.dto.response.CourseResponse;
import com.careercompass.security.UserPrincipal;
import com.careercompass.service.FavoriteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService service;

    @PostMapping("/careers/{onetCode}")
    public ApiResponse<Void> saveCareer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String onetCode) {
        service.saveCareer(principal.getId(), onetCode);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/careers/{onetCode}")
    public ApiResponse<Void> deleteCareer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String onetCode) {
        service.deleteCareer(principal.getId(), onetCode);
        return ApiResponse.success(null);
    }

    @GetMapping("/careers")
    public ApiResponse<List<CareerSummaryResponse>> careers(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(service.careers(principal.getId()));
    }

    @PostMapping("/courses/{courseId}")
    public ApiResponse<Void> saveCourse(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long courseId) {
        service.saveCourse(principal.getId(), courseId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/courses/{courseId}")
    public ApiResponse<Void> deleteCourse(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long courseId) {
        service.deleteCourse(principal.getId(), courseId);
        return ApiResponse.success(null);
    }

    @GetMapping("/courses")
    public ApiResponse<List<CourseResponse>> courses(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(service.courses(principal.getId()));
    }
}
