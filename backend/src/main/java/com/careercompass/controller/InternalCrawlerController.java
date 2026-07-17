package com.careercompass.controller;

import com.careercompass.dto.request.CompleteCrawlRunRequest;
import com.careercompass.dto.request.CreateCrawlRunRequest;
import com.careercompass.dto.request.JobPostingSkillImportRequest;
import com.careercompass.dto.request.JobPostingsBatchImportRequest;
import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.CrawlRunResponse;
import com.careercompass.dto.response.JobBatchImportResponse;
import com.careercompass.service.CrawlerImportService;
import com.careercompass.service.MarketSignalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/v1")
@RequiredArgsConstructor
public class InternalCrawlerController {

    private final CrawlerImportService crawlerImportService;
    private final MarketSignalService marketSignalService;

    @PostMapping("/crawler/runs")
    public ApiResponse<CrawlRunResponse> createRun(@Valid @RequestBody CreateCrawlRunRequest request) {
        return ApiResponse.success(crawlerImportService.createRun(request));
    }

    @PutMapping("/crawler/runs/{id}")
    public ApiResponse<CrawlRunResponse> completeRun(
            @PathVariable Long id,
            @Valid @RequestBody CompleteCrawlRunRequest request
    ) {
        return ApiResponse.success(crawlerImportService.completeRun(id, request));
    }

    @PostMapping("/jobs/batch")
    public ApiResponse<JobBatchImportResponse> importJobs(@Valid @RequestBody JobPostingsBatchImportRequest request) {
        return ApiResponse.success(crawlerImportService.importJobs(request));
    }

    @PostMapping("/jobs/{id}/skills")
    public ApiResponse<Void> replaceSkills(
            @PathVariable Long id,
            @Valid @RequestBody JobPostingSkillImportRequest request
    ) {
        crawlerImportService.replaceSkills(id, request);
        return ApiResponse.success(null);
    }

    @PostMapping("/market-signals/recalculate")
    public ApiResponse<Integer> recalculateMarketSignals() {
        return ApiResponse.success(marketSignalService.recalculate());
    }
}
