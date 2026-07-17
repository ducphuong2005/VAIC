package com.careercompass.controller;

import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.CareerDetailResponse;
import com.careercompass.dto.response.CareerElementResponse;
import com.careercompass.dto.response.CareerSummaryResponse;
import com.careercompass.dto.response.CareerTaskResponse;
import com.careercompass.dto.response.PageResponse;
import com.careercompass.dto.response.RelatedCareerResponse;
import com.careercompass.dto.response.TechnologySkillResponse;
import com.careercompass.service.CareerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    @GetMapping("/careers")
    public ApiResponse<PageResponse<CareerSummaryResponse>> careers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "titleVi") String sort
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(sort).ascending());
        return ApiResponse.success(careerService.search(q, cluster, pageable));
    }

    @GetMapping("/careers/search")
    public ApiResponse<PageResponse<CareerSummaryResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(careerService.search(q, null, PageRequest.of(page, Math.min(size, 100))));
    }

    @GetMapping("/careers/{onetCode}")
    public ApiResponse<CareerDetailResponse> detail(@PathVariable String onetCode) {
        return ApiResponse.success(careerService.detail(onetCode));
    }

    @GetMapping("/careers/{onetCode}/skills")
    public ApiResponse<List<TechnologySkillResponse>> skills(@PathVariable String onetCode) {
        return ApiResponse.success(careerService.technologySkills(onetCode));
    }

    @GetMapping("/careers/{onetCode}/tasks")
    public ApiResponse<List<CareerTaskResponse>> tasks(@PathVariable String onetCode) {
        return ApiResponse.success(careerService.tasks(onetCode));
    }

    @GetMapping("/careers/{onetCode}/related")
    public ApiResponse<List<RelatedCareerResponse>> related(@PathVariable String onetCode) {
        return ApiResponse.success(careerService.related(onetCode));
    }

    @GetMapping("/careers/{onetCode}/elements")
    public ApiResponse<List<CareerElementResponse>> elements(@PathVariable String onetCode) {
        return ApiResponse.success(careerService.elements(onetCode));
    }

    @GetMapping("/career-clusters")
    public ApiResponse<List<String>> careerClusters() {
        return ApiResponse.success(careerService.careerClusters());
    }
}
