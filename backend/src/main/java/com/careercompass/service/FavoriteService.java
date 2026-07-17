package com.careercompass.service;

import com.careercompass.dto.response.CareerSummaryResponse;
import com.careercompass.dto.response.CourseResponse;
import com.careercompass.entity.UserFavoriteCourse;
import com.careercompass.entity.UserFavoriteOccupation;
import com.careercompass.exception.ApiException;
import com.careercompass.mapper.OccupationMapper;
import com.careercompass.repository.CourseRepository;
import com.careercompass.repository.OccupationRepository;
import com.careercompass.repository.UserFavoriteCourseRepository;
import com.careercompass.repository.UserFavoriteOccupationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final UserFavoriteOccupationRepository occupationFavorites;
    private final UserFavoriteCourseRepository courseFavorites;
    private final OccupationRepository occupationRepository;
    private final CourseRepository courseRepository;
    private final OccupationMapper occupationMapper;
    private final ActivityLogService activityLogService;

    @Transactional
    public void saveCareer(UUID userId, String onetCode) {
        if (!occupationRepository.existsById(onetCode)) throw new ApiException(HttpStatus.NOT_FOUND, "Career not found");
        occupationFavorites.findByUserIdAndOnetCode(userId, onetCode).orElseGet(() -> occupationFavorites.save(UserFavoriteOccupation.builder().userId(userId).onetCode(onetCode).build()));
        activityLogService.log(userId, "CAREER_SAVED", "Career saved", onetCode);
    }

    @Transactional
    public void deleteCareer(UUID userId, String onetCode) {
        occupationFavorites.deleteByUserIdAndOnetCode(userId, onetCode);
    }

    public List<CareerSummaryResponse> careers(UUID userId) {
        return occupationFavorites.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(fav -> occupationRepository.findById(fav.getOnetCode()).orElse(null))
                .filter(occupation -> occupation != null)
                .map(occupationMapper::toSummary)
                .toList();
    }

    @Transactional
    public void saveCourse(UUID userId, Long courseId) {
        if (!courseRepository.existsById(courseId)) throw new ApiException(HttpStatus.NOT_FOUND, "Course not found");
        courseFavorites.findByUserIdAndCourseId(userId, courseId).orElseGet(() -> courseFavorites.save(UserFavoriteCourse.builder().userId(userId).courseId(courseId).build()));
    }

    @Transactional
    public void deleteCourse(UUID userId, Long courseId) {
        courseFavorites.deleteByUserIdAndCourseId(userId, courseId);
    }

    public List<CourseResponse> courses(UUID userId) {
        return courseFavorites.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(fav -> courseRepository.findById(fav.getCourseId()).orElse(null))
                .filter(course -> course != null)
                .map(course -> new CourseResponse(course.getId(), course.getTitle(), course.getDescription(), course.getRoute(), course.getDurationHours(), course.getUrl()))
                .toList();
    }
}
