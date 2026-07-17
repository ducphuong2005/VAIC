package com.careercompass.repository;

import com.careercompass.entity.DocumentType;
import com.careercompass.entity.RagDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {

    List<RagDocument> findByActiveTrue();

    List<RagDocument> findByActiveTrueAndDocumentType(DocumentType documentType);
}
