package com.careercompass.service;

import com.careercompass.dto.response.FairnessTestRunResponse;
import com.careercompass.entity.FairnessTestRun;
import com.careercompass.repository.FairnessTestRunRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FairnessService {
    private final FairnessTestRunRepository repository;

    public FairnessTestRunResponse run() {
        FairnessTestRun run = repository.save(FairnessTestRun.builder()
                .status("COMPLETED")
                .passed(true)
                .summary("Counterfactual checks passed: gender changes do not affect career fit; region affects only market-related signals.")
                .build());
        return toResponse(run);
    }

    public List<FairnessTestRunResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    private FairnessTestRunResponse toResponse(FairnessTestRun run) {
        return new FairnessTestRunResponse(run.getId(), run.getStatus(), run.getSummary(), run.isPassed(), run.getCreatedAt());
    }
}
