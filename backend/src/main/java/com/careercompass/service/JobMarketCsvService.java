package com.careercompass.service;

import com.careercompass.dto.response.AiCareerOptionResponse;
import com.careercompass.dto.response.SkillDemandResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JobMarketCsvService {

    private final String configuredPath;
    private volatile List<JobMarketSample> cache;

    public JobMarketCsvService(@Value("${app.market.jobs-csv-path:}") String configuredPath) {
        this.configuredPath = configuredPath;
    }

    public List<JobMarketSample> findRelevantSamples(
            List<AiCareerOptionResponse> careerOptions,
            List<String> topRiasecTypes,
            String preferredRegion,
            int limit
    ) {
        Set<String> keywords = buildKeywords(careerOptions, topRiasecTypes);
        if (keywords.isEmpty()) {
            keywords.addAll(List.of("dữ liệu", "phân tích", "data", "business analyst", "marketing"));
        }
        return samples().stream()
                .map(sample -> sample.withRelevance(relevance(sample, keywords, preferredRegion)))
                .filter(sample -> sample.relevanceScore() > 0)
                .sorted(Comparator
                        .comparing(JobMarketSample::relevanceScore).reversed()
                        .thenComparing(JobMarketSample::salaryMax, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(Math.max(0, limit))
                .toList();
    }

    public int sampleCount() {
        return samples().size();
    }

    public List<SkillDemandResponse> skillsInDemand(int limit) {
        Map<String, Long> counts = samples().stream()
                .flatMap(sample -> marketSkillTerms(sample).stream())
                .map(this::clean)
                .filter(StringUtils::hasText)
                .filter(value -> value.length() <= 70)
                .collect(java.util.stream.Collectors.groupingBy(
                        value -> normalize(value).replaceAll("\\s+", " "),
                        java.util.stream.Collectors.counting()
                ));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(Math.max(0, limit))
                .map(entry -> new SkillDemandResponse(displayTerm(entry.getKey()), entry.getValue(), "data/jobs.csv"))
                .toList();
    }

    private List<JobMarketSample> samples() {
        List<JobMarketSample> current = cache;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (cache == null) {
                cache = load(resolvePath());
            }
            return cache;
        }
    }

    private Path resolvePath() {
        List<Path> candidates = new ArrayList<>();
        if (StringUtils.hasText(configuredPath)) {
            candidates.add(Path.of(configuredPath));
        }
        candidates.add(Path.of("data/jobs.csv"));
        candidates.add(Path.of("../data/jobs.csv"));
        candidates.add(Path.of("../../data/jobs.csv"));
        return candidates.stream()
                .filter(path -> Files.isRegularFile(path.toAbsolutePath().normalize()))
                .findFirst()
                .orElse(null);
    }

    private List<JobMarketSample> load(Path path) {
        if (path == null) {
            return List.of();
        }
        List<JobMarketSample> samples = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return List.of();
            }
            String line;
            long row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                List<String> columns = parseCsvLine(line);
                if (columns.size() < 11) {
                    continue;
                }
                samples.add(new JobMarketSample(
                        row,
                        clean(columns.get(0)),
                        clean(columns.get(1)),
                        clean(columns.get(2)),
                        clean(columns.get(3)),
                        clean(columns.get(4)),
                        splitList(columns.get(5)),
                        splitList(columns.get(6)),
                        clean(columns.get(7)),
                        decimal(columns.get(8)),
                        decimal(columns.get(9)),
                        clean(columns.get(10)),
                        0
                ));
            }
        } catch (IOException exception) {
            return List.of();
        }
        return List.copyOf(samples);
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                result.add(value.toString());
                value.setLength(0);
            } else {
                value.append(ch);
            }
        }
        result.add(value.toString());
        return result;
    }

    private Set<String> buildKeywords(List<AiCareerOptionResponse> careerOptions, List<String> topRiasecTypes) {
        Set<String> keywords = new LinkedHashSet<>();
        for (AiCareerOptionResponse option : careerOptions) {
            addKeyword(keywords, option.titleVi());
            addKeyword(keywords, option.titleEn());
            addDomainKeywords(keywords, normalize(option.titleVi() + " " + option.titleEn() + " " + option.reason()));
        }
        for (String riasec : topRiasecTypes) {
            addDomainKeywords(keywords, normalize(riasec));
        }
        return keywords;
    }

    private void addDomainKeywords(Set<String> keywords, String text) {
        if (containsAny(text, "data", "dữ liệu", "investigative", "thinkers", "phân tích", "nghiên cứu")) {
            keywords.addAll(List.of(
                    "data analyst", "business analyst", "dữ liệu", "phân tích dữ liệu",
                    "xử lý dữ liệu", "thống kê", "sql", "python", "market research"
            ));
        }
        if (containsAny(text, "software", "phần mềm", "developer", "lập trình")) {
            keywords.addAll(List.of("software", "developer", "lập trình", "cntt - phần mềm", "javascript", "java", "backend", "frontend"));
        }
        if (containsAny(text, "marketing", "thị trường", "market")) {
            keywords.addAll(List.of("marketing", "digital marketing", "market research", "nghiên cứu thị trường", "performance marketing"));
        }
        if (containsAny(text, "conventional", "organizers", "quy trình", "hồ sơ", "chi tiết")) {
            keywords.addAll(List.of("nhập liệu", "kế toán", "kiểm toán", "thống kê", "dữ liệu", "vận hành"));
        }
        if (containsAny(text, "enterprising", "persuaders", "dẫn dắt", "thuyết phục")) {
            keywords.addAll(List.of("kinh doanh", "sales", "quản lý", "trưởng nhóm", "tư vấn"));
        }
        if (containsAny(text, "artistic", "creators", "sáng tạo")) {
            keywords.addAll(List.of("thiết kế", "content", "creative", "mỹ thuật", "truyền thông"));
        }
        if (containsAny(text, "social", "helpers", "hỗ trợ", "giảng dạy")) {
            keywords.addAll(List.of("tư vấn", "giáo dục", "đào tạo", "dịch vụ khách hàng", "chăm sóc"));
        }
        if (containsAny(text, "realistic", "doers", "thực hành", "máy móc")) {
            keywords.addAll(List.of("kỹ thuật", "cơ khí", "vận hành", "sản xuất", "xây dựng"));
        }
    }

    private int relevance(JobMarketSample sample, Set<String> keywords, String preferredRegion) {
        String text = sample.searchText();
        int score = 0;
        for (String keyword : keywords) {
            String normalized = normalize(keyword);
            if (normalized.length() >= 3 && text.contains(normalized)) {
                score += normalized.length() > 10 ? 6 : 3;
            }
        }
        if (StringUtils.hasText(preferredRegion) && text.contains(normalize(preferredRegion))) {
            score += 5;
        }
        if (containsAny(sample.positionLevel(), "nhân viên", "sinh viên", "thực tập")) {
            score += 2;
        }
        return score;
    }

    private void addKeyword(Set<String> keywords, String value) {
        for (String token : splitList(value)) {
            String normalized = normalize(token);
            if (normalized.length() >= 3) {
                keywords.add(normalized);
            }
        }
    }

    private List<String> splitList(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(this::clean)
                .filter(StringUtils::hasText)
                .limit(20)
                .toList();
    }

    private List<String> marketSkillTerms(JobMarketSample sample) {
        List<String> skills = sample.skills().stream()
                .filter(value -> StringUtils.hasText(value) && value.length() <= 70)
                .toList();
        return skills.isEmpty() ? sample.fields() : skills;
    }

    private String displayTerm(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.length() <= 3) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal decimal(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean containsAny(String text, String... needles) {
        String normalized = normalize(text);
        for (String needle : needles) {
            if (normalized.contains(normalize(needle))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record JobMarketSample(
            long rowNumber,
            String jobTitle,
            String jobType,
            String positionLevel,
            String city,
            String experience,
            List<String> skills,
            List<String> fields,
            String salary,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String unit,
            int relevanceScore
    ) {
        public JobMarketSample withRelevance(int relevanceScore) {
            return new JobMarketSample(rowNumber, jobTitle, jobType, positionLevel, city, experience, skills, fields, salary, salaryMin, salaryMax, unit, relevanceScore);
        }

        private String searchText() {
            return String.join(" ",
                    jobTitle,
                    jobType,
                    positionLevel,
                    city,
                    experience,
                    String.join(" ", skills),
                    String.join(" ", fields),
                    salary
            ).toLowerCase(Locale.ROOT);
        }
    }
}
