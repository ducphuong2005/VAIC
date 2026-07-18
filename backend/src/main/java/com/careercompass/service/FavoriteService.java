package com.careercompass.service;

import com.careercompass.dto.request.FavoriteLinkRequest;
import com.careercompass.dto.response.CareerSummaryResponse;
import com.careercompass.dto.response.CourseResponse;
import com.careercompass.dto.response.FavoriteLinkResponse;
import com.careercompass.entity.UserFavoriteCourse;
import com.careercompass.entity.UserFavoriteLink;
import com.careercompass.entity.UserFavoriteOccupation;
import com.careercompass.exception.ApiException;
import com.careercompass.mapper.OccupationMapper;
import com.careercompass.repository.CourseRepository;
import com.careercompass.repository.OccupationRepository;
import com.careercompass.repository.UserFavoriteCourseRepository;
import com.careercompass.repository.UserFavoriteLinkRepository;
import com.careercompass.repository.UserFavoriteOccupationRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final UserFavoriteOccupationRepository occupationFavorites;
    private final UserFavoriteCourseRepository courseFavorites;
    private final UserFavoriteLinkRepository linkFavorites;
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

    @Transactional
    public FavoriteLinkResponse saveLink(UUID userId, FavoriteLinkRequest request) {
        String url = normalizeUrl(request.url());
        UserFavoriteLink favorite = linkFavorites.findByUserIdAndUrl(userId, url)
                .orElseGet(() -> linkFavorites.save(UserFavoriteLink.builder()
                        .userId(userId)
                        .linkType(clean(request.linkType(), "LINK").toUpperCase(Locale.ROOT))
                        .provider(clean(request.provider(), "Link"))
                        .title(clean(request.title(), "Liên kết đã lưu"))
                        .url(url)
                        .description(StringUtils.hasText(request.description()) ? request.description().trim() : null)
                        .build()));
        activityLogService.log(userId, "LINK_SAVED", "Favorite link saved", url);
        return toLinkResponse(favorite);
    }

    @Transactional
    public void deleteLink(UUID userId, Long id) {
        linkFavorites.deleteByUserIdAndId(userId, id);
    }

    public List<FavoriteLinkResponse> links(UUID userId) {
        return linkFavorites.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toLinkResponse)
                .toList();
    }

    private FavoriteLinkResponse toLinkResponse(UserFavoriteLink favorite) {
        return new FavoriteLinkResponse(
                favorite.getId(),
                favorite.getLinkType(),
                favorite.getProvider(),
                favorite.getTitle(),
                favorite.getUrl(),
                favorite.getDescription(),
                favorite.getCreatedAt()
        );
    }

    private String normalizeUrl(String value) {
        if (!StringUtils.hasText(value)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Favorite link URL is required");
        }
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();
            if (!StringUtils.hasText(scheme)
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
                    || !StringUtils.hasText(uri.getHost())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Favorite link URL must be http/https");
            }
            return uri.toString();
        } catch (URISyntaxException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Favorite link URL is invalid");
        }
    }

    private String clean(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
