package com.careercompass.service;

import com.careercompass.dto.response.ActivityLogResponse;
import com.careercompass.entity.ActivityLog;
import com.careercompass.repository.ActivityLogRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final ActivityLogRepository repository;

    public void log(UUID userId, String type, String message, String metadata) {
        repository.save(ActivityLog.builder().userId(userId).activityType(type).message(message).metadata(metadata).build());
    }

    public List<ActivityLogResponse> list(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(log -> new ActivityLogResponse(log.getId(), log.getActivityType(), log.getMessage(), log.getMetadata(), log.getCreatedAt()))
                .toList();
    }
}
