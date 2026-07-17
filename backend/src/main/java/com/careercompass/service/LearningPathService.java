package com.careercompass.service;

import com.careercompass.dto.request.GenerateLearningPathRequest;
import com.careercompass.dto.request.UpdateStepProgressRequest;
import com.careercompass.dto.response.LearningPathResponse;
import com.careercompass.dto.response.LearningPathStepResponse;
import com.careercompass.entity.LearningPath;
import com.careercompass.entity.LearningPathStep;
import com.careercompass.exception.ApiException;
import com.careercompass.repository.CourseRepository;
import com.careercompass.repository.LearningPathRepository;
import com.careercompass.repository.LearningPathStepRepository;
import com.careercompass.repository.OccupationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningPathService {
    private final LearningPathRepository pathRepository;
    private final LearningPathStepRepository stepRepository;
    private final OccupationRepository occupationRepository;
    private final CourseRepository courseRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public LearningPathResponse generate(UUID userId, GenerateLearningPathRequest request) {
        var occupation = occupationRepository.findById(request.onetCode()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Career not found"));
        String route = request.route() == null ? "SELF_STUDY" : request.route();
        LearningPath path = pathRepository.save(LearningPath.builder()
                .userId(userId)
                .onetCode(request.onetCode())
                .title("Lộ trình tới " + occupation.getTitleVi())
                .route(route)
                .status("ACTIVE")
                .build());
        List<LearningPathStep> steps = List.of(
                step(path.getId(), 1, "Củng cố nền tảng", "Ôn lại kỹ năng nền tảng và thuật ngữ nghề.", "Foundations", 1L, 24),
                step(path.getId(), 2, "Thực hành kỹ năng chính", "Làm project nhỏ liên quan nghề mục tiêu.", "Practice", 2L, 36),
                step(path.getId(), 3, "Xây portfolio", "Tổng hợp sản phẩm và chuẩn bị phỏng vấn.", "Portfolio", 3L, 30)
        );
        stepRepository.saveAll(steps);
        activityLogService.log(userId, "LEARNING_PATH_GENERATED", "Learning path generated", path.getId().toString());
        return toResponse(path);
    }

    public List<LearningPathResponse> list(UUID userId) {
        return pathRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    public LearningPathResponse get(UUID userId, UUID id) {
        return toResponse(owned(userId, id));
    }

    @Transactional
    public LearningPathResponse updateStep(UUID userId, UUID pathId, Long stepId, UpdateStepProgressRequest request) {
        LearningPath path = owned(userId, pathId);
        LearningPathStep step = stepRepository.findByIdAndLearningPathId(stepId, pathId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Step not found"));
        step.setProgressPercent(request.progressPercent());
        step.setCompleted(request.progressPercent() >= 100);
        stepRepository.save(step);
        if (step.isCompleted()) {
            activityLogService.log(userId, "LEARNING_STEP_COMPLETED", "Learning step completed", stepId.toString());
        }
        return toResponse(path);
    }

    private LearningPath owned(UUID userId, UUID id) {
        return pathRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Learning path not found"));
    }

    private LearningPathStep step(UUID pathId, int order, String title, String desc, String skill, Long courseId, int hours) {
        if (!courseRepository.existsById(courseId)) courseId = null;
        return LearningPathStep.builder().learningPathId(pathId).stepOrder(order).title(title).description(desc).targetSkill(skill).courseId(courseId).durationHours(hours).progressPercent(0).completed(false).build();
    }

    private LearningPathResponse toResponse(LearningPath path) {
        return new LearningPathResponse(path.getId(), path.getOnetCode(), path.getTitle(), path.getRoute(), path.getStatus(),
                stepRepository.findByLearningPathIdOrderByStepOrderAsc(path.getId()).stream().map(this::toStep).toList());
    }

    private LearningPathStepResponse toStep(LearningPathStep step) {
        return new LearningPathStepResponse(step.getId(), step.getStepOrder(), step.getTitle(), step.getDescription(), step.getTargetSkill(), step.getCourseId(), step.getDurationHours(), step.getProgressPercent(), step.isCompleted());
    }
}
