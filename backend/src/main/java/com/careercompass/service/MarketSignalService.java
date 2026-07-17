package com.careercompass.service;

import com.careercompass.dto.response.MarketCareerResponse;
import com.careercompass.dto.response.MarketSignalResponse;
import com.careercompass.dto.response.SkillDemandResponse;
import com.careercompass.dto.response.TrendingCareerResponse;
import com.careercompass.entity.JobPosting;
import com.careercompass.entity.MarketSignal;
import com.careercompass.entity.Occupation;
import com.careercompass.exception.ApiException;
import com.careercompass.repository.JobPostingRepository;
import com.careercompass.repository.JobPostingSkillRepository;
import com.careercompass.repository.MarketSignalRepository;
import com.careercompass.repository.OccupationRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MarketSignalService {

    private static final String TOPCV_SOURCE = "TopCV";
    private static final String CURRENT_PERIOD = "CURRENT";
    private static final String LIMITATION = "TopCV data is one market sample, not the whole Vietnam labor market.";

    private final JobPostingRepository jobPostingRepository;
    private final JobPostingSkillRepository skillRepository;
    private final MarketSignalRepository marketSignalRepository;
    private final OccupationRepository occupationRepository;

    @Transactional
    public int recalculate() {
        List<JobPosting> postings = jobPostingRepository.findByActiveTrueAndOnetCodeIsNotNull();
        Map<String, List<JobPosting>> groups = postings.stream()
                .collect(Collectors.groupingBy(posting -> posting.getOnetCode() + "||" + normalizeRegion(posting.getRegion())));

        int saved = 0;
        for (Map.Entry<String, List<JobPosting>> entry : groups.entrySet()) {
            String[] key = entry.getKey().split("\\|\\|", 2);
            String onetCode = key[0];
            String region = key[1];
            List<JobPosting> group = entry.getValue();
            MarketSignal signal = marketSignalRepository
                    .findByOnetCodeAndRegionAndSourceAndPeriodLabel(onetCode, region, TOPCV_SOURCE, CURRENT_PERIOD)
                    .orElseGet(MarketSignal::new);
            signal.setOnetCode(onetCode);
            signal.setRegion(region);
            signal.setSource(TOPCV_SOURCE);
            signal.setPeriodLabel(CURRENT_PERIOD);
            signal.setJobCount(group.size());
            signal.setGrowthRate(BigDecimal.ZERO);
            signal.setMedianSalary(medianSalary(group));
            signal.setEntryLevelRatio(ratio(group.stream().filter(JobPosting::isEntryLevel).count(), group.size()));
            signal.setRemoteRatio(ratio(group.stream().filter(JobPosting::isRemote).count(), group.size()));
            signal.setDemandScore(demandScore(group.size()));
            signal.setDataConfidence(dataConfidence(group.size()));
            signal.setSampleSize(group.size());
            signal.setLastUpdatedAt(Instant.now());
            marketSignalRepository.save(signal);
            saved++;
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<TrendingCareerResponse> trending(int limit) {
        Map<String, Occupation> occupations = occupationRepository.findByActiveTrueOrderByTitleViAsc().stream()
                .collect(Collectors.toMap(Occupation::getOnetCode, Function.identity()));
        return jobPostingRepository.findTrendingOnetCodes(PageRequest.of(0, limit)).stream()
                .map(row -> {
                    String onetCode = (String) row[0];
                    long count = (Long) row[1];
                    Occupation occupation = occupations.get(onetCode);
                    return new TrendingCareerResponse(
                            onetCode,
                            occupation == null ? onetCode : occupation.getTitleVi(),
                            occupation == null ? onetCode : occupation.getTitleEn(),
                            count,
                            TOPCV_SOURCE,
                            LIMITATION
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SkillDemandResponse> skillsInDemand(int limit) {
        return skillRepository.findSkillsInDemand(PageRequest.of(0, limit)).stream()
                .map(row -> new SkillDemandResponse((String) row[0], (Long) row[1], TOPCV_SOURCE))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> regions() {
        return jobPostingRepository.findActiveRegions();
    }

    @Transactional(readOnly = true)
    public MarketCareerResponse careerMarket(String onetCode) {
        ensureCareerExists(onetCode);
        return new MarketCareerResponse(onetCode, careerTrend(onetCode, null), LIMITATION);
    }

    @Transactional(readOnly = true)
    public List<MarketSignalResponse> careerTrend(String onetCode, String region) {
        ensureCareerExists(onetCode);
        List<MarketSignal> signals = StringUtils.hasText(region)
                ? marketSignalRepository.findByOnetCodeAndRegionOrderByLastUpdatedAtDesc(onetCode, region.trim())
                : marketSignalRepository.findByOnetCodeOrderByLastUpdatedAtDesc(onetCode);
        return signals.stream().map(this::toSignalResponse).toList();
    }

    private void ensureCareerExists(String onetCode) {
        if (!occupationRepository.existsById(onetCode)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Career not found");
        }
    }

    private MarketSignalResponse toSignalResponse(MarketSignal signal) {
        return new MarketSignalResponse(
                signal.getSource(),
                signal.getLastUpdatedAt(),
                signal.getDataConfidence(),
                signal.getSampleSize(),
                signal.getJobCount(),
                signal.getGrowthRate(),
                signal.getMedianSalary(),
                signal.getEntryLevelRatio(),
                signal.getRemoteRatio(),
                signal.getDemandScore()
        );
    }

    private String normalizeRegion(String region) {
        return StringUtils.hasText(region) ? region.trim() : "UNKNOWN";
    }

    private BigDecimal medianSalary(List<JobPosting> postings) {
        List<BigDecimal> salaries = postings.stream()
                .map(this::salaryMidpoint)
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .toList();
        if (salaries.isEmpty()) {
            return null;
        }
        int middle = salaries.size() / 2;
        if (salaries.size() % 2 == 1) {
            return salaries.get(middle);
        }
        return salaries.get(middle - 1).add(salaries.get(middle)).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal salaryMidpoint(JobPosting posting) {
        if (posting.getSalaryMin() == null && posting.getSalaryMax() == null) {
            return null;
        }
        if (posting.getSalaryMin() == null) {
            return posting.getSalaryMax();
        }
        if (posting.getSalaryMax() == null) {
            return posting.getSalaryMin();
        }
        return posting.getSalaryMin().add(posting.getSalaryMax()).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal ratio(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal demandScore(int sampleSize) {
        return BigDecimal.valueOf(Math.min(100, sampleSize * 10L));
    }

    private BigDecimal dataConfidence(int sampleSize) {
        return BigDecimal.valueOf(Math.min(90, 30 + sampleSize * 10L));
    }
}
