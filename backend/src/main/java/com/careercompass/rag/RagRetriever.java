package com.careercompass.rag;

import java.util.List;

public interface RagRetriever {

    List<RagDocumentResult> retrieve(RagQuery query);
}
