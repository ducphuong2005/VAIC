package com.careercompass.service;

import com.careercompass.dto.request.CompleteCrawlRunRequest;
import com.careercompass.dto.request.CreateCrawlRunRequest;
import com.careercompass.dto.request.JobPostingImportRequest;
import com.careercompass.dto.request.JobPostingSkillImportRequest;
import com.careercompass.dto.request.JobPostingsBatchImportRequest;
import com.careercompass.dto.response.CrawlRunResponse;
import com.careercompass.dto.response.JobBatchImportResponse;
import com.careercompass.entity.CrawlRun;
import com.careercompass.entity.CrawlRunStatus;
import com.careercompass.entity.JobPosting;
import com.careercompass.entity.JobPostingRaw;
import com.careercompass.entity.JobPostingSkill;
import com.careercompass.exception.ApiException;
import com.careercompass.repository.CrawlRunRepository;
import com.careercompass.repository.JobPostingRawRepository;
import com.careercompass.repository.JobPostingRepository;
import com.careercompass.repository.JobPostingSkillRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CrawlerImportService {

    private final CrawlRunRepository crawlRunRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingSkillRepository skillRepository;
    private final JobPostingRawRepository rawRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public CrawlRunResponse createRun(CreateCrawlRunRequest request) {
        CrawlRun run = CrawlRun.builder()
                .sourceName(request.sourceName().trim())
                .status(CrawlRunStatus.RUNNING)
                .startedAt(Instant.now())
                .build();
        return toRunResponse(crawlRunRepository.save(run));
    }

    @Transactional
    public CrawlRunResponse completeRun(Long id, CompleteCrawlRunRequest request) {
        CrawlRun run = crawlRunRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Crawl run not found"));
        run.setStatus(request.status());
        run.setFinishedAt(Instant.now());
        run.setErrorMessage(request.errorMessage());
        return toRunResponse(crawlRunRepository.save(run));
    }

    @Transactional
    public JobBatchImportResponse importJobs(JobPostingsBatchImportRequest request) {
        int inserted = 0;
        int updated = 0;
        int duplicate = 0;
        int rejected = 0;

        for (JobPostingImportRequest jobRequest : request.jobs()) {
            rawRepository.save(JobPostingRaw.builder()
                    .crawlRunId(request.crawlRunId())
                    .sourceName(jobRequest.sourceName())
                    .externalId(jobRequest.externalId())
                    .contentHash(jobRequest.contentHash())
                    .rawPayload(toJson(jobRequest))
                    .accepted(true)
                    .build());

            if (!StringUtils.hasText(jobRequest.onetCode())) {
                rejected++;
                continue;
            }

            JobPosting existing = jobPostingRepository.findBySourceNameAndExternalId(jobRequest.sourceName(), jobRequest.externalId())
                    .orElse(null);
            if (existing == null && jobPostingRepository.existsByContentHash(jobRequest.contentHash())) {
                duplicate++;
                continue;
            }

            JobPosting saved = existing == null
                    ? jobPostingRepository.save(toJobPosting(jobRequest))
                    : jobPostingRepository.save(updateJobPosting(existing, jobRequest));
            replaceSkills(saved.getId(), jobRequest.skills());
            if (existing == null) {
                inserted++;
            } else {
                updated++;
            }
        }

        if (request.crawlRunId() != null) {
            CrawlRun run = crawlRunRepository.findById(request.crawlRunId()).orElse(null);
            if (run != null) {
                run.setInsertedCount(run.getInsertedCount() + inserted);
                run.setUpdatedCount(run.getUpdatedCount() + updated);
                run.setDuplicateCount(run.getDuplicateCount() + duplicate);
                run.setRejectedCount(run.getRejectedCount() + rejected);
                crawlRunRepository.save(run);
            }
        }

        return new JobBatchImportResponse(inserted, updated, duplicate, rejected);
    }

    @Transactional
    public void replaceSkills(Long jobPostingId, JobPostingSkillImportRequest request) {
        replaceSkills(jobPostingId, request.skills());
    }

    private void replaceSkills(Long jobPostingId, List<String> skills) {
        skillRepository.deleteByJobPostingId(jobPostingId);
        if (skills == null || skills.isEmpty()) {
            return;
        }
        List<JobPostingSkill> entities = skills.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .map(skill -> JobPostingSkill.builder()
                        .jobPostingId(jobPostingId)
                        .skillName(skill)
                        .normalizedSkillName(normalizeSkill(skill))
                        .build())
                .toList();
        skillRepository.saveAll(entities);
    }

    private JobPosting toJobPosting(JobPostingImportRequest request) {
        return updateJobPosting(JobPosting.builder().active(true).build(), request);
    }

    private JobPosting updateJobPosting(JobPosting posting, JobPostingImportRequest request) {
        posting.setSourceName(request.sourceName().trim());
        posting.setExternalId(request.externalId().trim());
        posting.setContentHash(request.contentHash().trim());
        posting.setTitle(request.title().trim());
        posting.setCompanyName(trim(request.companyName()));
        posting.setLocation(trim(request.location()));
        posting.setRegion(trim(request.region()));
        posting.setSalaryMin(request.salaryMin());
        posting.setSalaryMax(request.salaryMax());
        posting.setRemote(request.remote());
        posting.setEntryLevel(request.entryLevel());
        posting.setOnetCode(trim(request.onetCode()));
        posting.setPostedAt(request.postedAt());
        posting.setCrawledAt(Instant.now());
        posting.setRawUrl(trim(request.rawUrl()));
        posting.setDescription(trim(request.description()));
        posting.setActive(true);
        return posting;
    }

    private CrawlRunResponse toRunResponse(CrawlRun run) {
        return new CrawlRunResponse(
                run.getId(),
                run.getSourceName(),
                run.getStatus().name(),
                run.getStartedAt(),
                run.getFinishedAt(),
                run.getInsertedCount(),
                run.getUpdatedCount(),
                run.getDuplicateCount(),
                run.getRejectedCount(),
                run.getErrorMessage()
        );
    }

    private String normalizeSkill(String skill) {
        return skill.trim().toLowerCase(Locale.ROOT);
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }
}
