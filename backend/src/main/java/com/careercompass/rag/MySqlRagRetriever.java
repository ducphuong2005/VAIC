package com.careercompass.rag;

import com.careercompass.entity.RagDocument;
import com.careercompass.repository.RagDocumentRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class MySqlRagRetriever implements RagRetriever {

    private final RagDocumentRepository repository;

    @Override
    public List<RagDocumentResult> retrieve(RagQuery query) {
        String normalized = query.query() == null ? "" : query.query().toLowerCase(Locale.ROOT);
        return repository.findByActiveTrue().stream()
                .filter(document -> !StringUtils.hasText(query.onetCode()) || query.onetCode().equals(document.getOnetCode()))
                .filter(document -> !StringUtils.hasText(query.language()) || query.language().equalsIgnoreCase(document.getLanguage()))
                .filter(document -> !StringUtils.hasText(query.region()) || document.getRegion() == null || query.region().equalsIgnoreCase(document.getRegion()))
                .filter(document -> !StringUtils.hasText(query.sourceVersion()) || query.sourceVersion().equalsIgnoreCase(document.getSourceVersion()))
                .filter(document -> query.documentType() == null || query.documentType() == document.getDocumentType())
                .map(document -> toResult(document, normalized))
                .sorted(Comparator.comparing(RagDocumentResult::score).reversed())
                .limit(query.limit() <= 0 ? 5 : query.limit())
                .toList();
    }

    private RagDocumentResult toResult(RagDocument document, String query) {
        double score = 0;
        String title = document.getTitle().toLowerCase(Locale.ROOT);
        String content = document.getContent().toLowerCase(Locale.ROOT);
        for (String token : query.split("\\s+")) {
            if (StringUtils.hasText(token)) {
                if (title.contains(token)) {
                    score += 3;
                }
                if (content.contains(token)) {
                    score += 1;
                }
            }
        }
        return new RagDocumentResult(document.getId(), document.getTitle(), document.getContent(), document.getDocumentType().name(), document.getOnetCode(), score);
    }
}
