package com.careercompass.service;

import com.careercompass.dto.response.LearningResourceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LearningResourceService {

    private static final List<String> ALLOWED_URL_PREFIXES = List.of(
            "https://www.coursera.org/",
            "https://coursera.org/",
            "https://www.w3schools.com/",
            "https://w3schools.com/",
            "https://www.freecodecamp.org/",
            "https://developers.google.com/",
            "https://skillshop.withgoogle.com/"
    );

    private final ObjectMapper objectMapper;

    public String resourceLinksPayload(
            String targetSkill,
            String careerTitle,
            String stepText,
            List<LearningResourceResponse> llmResources
    ) {
        return toJson(resourcesForStep(targetSkill, careerTitle, stepText, llmResources));
    }

    public List<LearningResourceResponse> resourcesForStep(
            String targetSkill,
            String careerTitle,
            String stepText,
            List<LearningResourceResponse> llmResources
    ) {
        String skill = fallback(targetSkill, "Kỹ năng nghề nghiệp");
        List<LearningResourceResponse> resources = new ArrayList<>();
        for (LearningResourceResponse resource : llmResources == null ? List.<LearningResourceResponse>of() : llmResources) {
            if (resources.size() >= 3) {
                break;
            }
            sanitize(resource, skill).ifPresent(candidate -> {
                if (matches(candidate, skill, stepText, careerTitle)) {
                    resources.add(candidate);
                }
            });
        }
        addIfMissing(resources, w3SchoolsResource(skill, stepText, careerTitle));
        addIfMissing(resources, courseraResource(skill, careerTitle));
        return resources.stream().limit(3).toList();
    }

    public List<LearningResourceResponse> parse(String payload) {
        if (!StringUtils.hasText(payload)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    public List<LearningResourceResponse> parseFromLlm(Object rawValue) {
        if (rawValue == null) {
            return List.of();
        }
        try {
            return objectMapper.convertValue(rawValue, new TypeReference<List<LearningResourceResponse>>() {
            });
        } catch (IllegalArgumentException exception) {
            return List.of();
        }
    }

    private java.util.Optional<LearningResourceResponse> sanitize(LearningResourceResponse resource, String fallbackSkill) {
        if (resource == null || !isAllowedUrl(resource.url())) {
            return java.util.Optional.empty();
        }
        String provider = fallback(resource.provider(), providerFromUrl(resource.url()));
        String title = fallback(resource.title(), provider + " - " + fallbackSkill);
        String targetSkill = fallback(resource.targetSkill(), fallbackSkill);
        String reason = fallback(resource.reason(), "Phù hợp với kỹ năng cần học trong lộ trình.");
        return java.util.Optional.of(new LearningResourceResponse(provider, limit(title, 180), resource.url(), limit(targetSkill, 120), limit(reason, 240)));
    }

    private boolean matches(LearningResourceResponse resource, String targetSkill, String stepText, String careerTitle) {
        String haystack = normalize(resource.targetSkill() + " " + resource.title() + " " + resource.reason());
        String target = normalize(targetSkill);
        if (StringUtils.hasText(target) && haystack.contains(target)) {
            return true;
        }
        String context = normalize(stepText + " " + careerTitle);
        return context.contains(normalize(resource.targetSkill())) || resourcesProvider(resource.provider());
    }

    private void addIfMissing(List<LearningResourceResponse> resources, LearningResourceResponse resource) {
        boolean exists = resources.stream().anyMatch(item -> item.url().equalsIgnoreCase(resource.url()));
        if (!exists) {
            resources.add(resource);
        }
    }

    private LearningResourceResponse courseraResource(String targetSkill, String careerTitle) {
        String topic = recommendedTopic(targetSkill, careerTitle, "");
        String query = normalizeQuery(topic);
        String url = "https://www.coursera.org/courses?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        return new LearningResourceResponse(
                "Coursera",
                "Tìm khóa học Coursera về " + displaySkill(topic),
                url,
                displaySkill(topic),
                "Dùng để chọn khóa học/certificate phù hợp với mục tiêu nghề nghiệp và thời lượng của bạn."
        );
    }

    private LearningResourceResponse w3SchoolsResource(String targetSkill, String stepText, String careerTitle) {
        String topic = recommendedTopic(targetSkill, careerTitle, stepText);
        String context = normalize(topic + " " + targetSkill + " " + stepText + " " + careerTitle);
        Map<String, String> tutorials = new LinkedHashMap<>();
        tutorials.put("pandas", "https://www.w3schools.com/python/pandas/default.asp");
        tutorials.put("numpy", "https://www.w3schools.com/python/numpy/default.asp");
        tutorials.put("mysql", "https://www.w3schools.com/mysql/");
        tutorials.put("sql", "https://www.w3schools.com/sql/");
        tutorials.put("data scientist", "https://www.w3schools.com/datascience/");
        tutorials.put("data science", "https://www.w3schools.com/datascience/");
        tutorials.put("phan tich du lieu", "https://www.w3schools.com/datascience/");
        tutorials.put("khoa hoc du lieu", "https://www.w3schools.com/datascience/");
        tutorials.put("du lieu", "https://www.w3schools.com/datascience/");
        tutorials.put("python", "https://www.w3schools.com/python/");
        tutorials.put("javascript", "https://www.w3schools.com/js/");
        tutorials.put("react", "https://www.w3schools.com/react/");
        tutorials.put("html", "https://www.w3schools.com/html/");
        tutorials.put("css", "https://www.w3schools.com/css/");
        tutorials.put("git", "https://www.w3schools.com/git/");
        tutorials.put("excel", "https://www.w3schools.com/excel/");
        tutorials.put("machine learning", "https://www.w3schools.com/ai/");
        tutorials.put("hoc may", "https://www.w3schools.com/ai/");
        tutorials.put("ai", "https://www.w3schools.com/ai/");
        String url = tutorials.entrySet().stream()
                .filter(entry -> context.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("https://www.w3schools.com/");
        return new LearningResourceResponse(
                "W3Schools",
                "Lesson thực hành W3Schools cho " + displaySkill(topic),
                url,
                displaySkill(topic),
                "Phù hợp để học nhanh bằng ví dụ, quiz và bài tập thực hành."
        );
    }

    private boolean isAllowedUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        return ALLOWED_URL_PREFIXES.stream().anyMatch(prefix -> url.startsWith(prefix));
    }

    private boolean resourcesProvider(String provider) {
        String normalized = normalize(provider);
        return normalized.contains("coursera") || normalized.contains("w3schools");
    }

    private String providerFromUrl(String url) {
        String normalized = normalize(url);
        if (normalized.contains("coursera.org")) {
            return "Coursera";
        }
        if (normalized.contains("w3schools.com")) {
            return "W3Schools";
        }
        if (normalized.contains("freecodecamp.org")) {
            return "freeCodeCamp";
        }
        if (normalized.contains("google")) {
            return "Google";
        }
        return "Online course";
    }

    private String displaySkill(String value) {
        return fallback(value, "kỹ năng nghề nghiệp").replaceAll("\\s+", " ").trim();
    }

    private String recommendedTopic(String targetSkill, String careerTitle, String stepText) {
        String skill = fallback(targetSkill, "");
        String context = normalize(skill + " " + careerTitle + " " + stepText);
        if (!isGenericSkill(skill)) {
            return skill;
        }
        if (containsAny(context, "data scientist", "data science", "khoa hoc du lieu", "phan tich du lieu", "du lieu")) {
            return "data science";
        }
        if (containsAny(context, "machine learning", "hoc may", "artificial intelligence")) {
            return "machine learning";
        }
        if (containsAny(context, "software", "developer", "lap trinh", "frontend", "backend")) {
            return "software development";
        }
        if (containsAny(context, "marketing", "digital marketing")) {
            return "digital marketing";
        }
        if (containsAny(context, "business", "kinh doanh", "product", "manager")) {
            return "business analytics";
        }
        if (StringUtils.hasText(careerTitle)) {
            return careerTitle + " foundations";
        }
        return "career skills";
    }

    private boolean isGenericSkill(String value) {
        String normalized = normalize(value);
        return !StringUtils.hasText(normalized)
                || List.of(
                        "foundations",
                        "foundation",
                        "fundamentals",
                        "basics",
                        "portfolio",
                        "interview",
                        "ky nang nghe nghiep",
                        "nen tang",
                        "co ban"
                ).contains(normalized);
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeQuery(String value) {
        return normalize(value).replaceAll("[^\\p{L}\\p{N}\\s+.-]", " ").replaceAll("\\s+", " ").trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String fallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String limit(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 1)).trim();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }
}
