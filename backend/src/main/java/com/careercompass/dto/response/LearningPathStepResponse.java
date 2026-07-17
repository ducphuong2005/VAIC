package com.careercompass.dto.response;

public record LearningPathStepResponse(Long id, int stepOrder, String title, String description, String targetSkill, Long courseId, Integer durationHours, int progressPercent, boolean completed) {
}
