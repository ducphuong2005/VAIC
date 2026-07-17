package com.careercompass.dto.response;

public record CourseResponse(Long id, String title, String description, String route, Integer durationHours, String url) {
}
