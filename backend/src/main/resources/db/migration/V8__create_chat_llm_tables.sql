CREATE TABLE conversation_sessions (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_conversation_sessions PRIMARY KEY (id),
    CONSTRAINT fk_conversation_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_conversation_sessions_user ON conversation_sessions(user_id, updated_at);

CREATE TABLE conversation_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id CHAR(36) NOT NULL,
    sender VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    intent VARCHAR(80),
    confidence DECIMAL(5,2),
    sources TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_conversation_messages PRIMARY KEY (id),
    CONSTRAINT fk_conversation_messages_session FOREIGN KEY (session_id) REFERENCES conversation_sessions(id),
    CONSTRAINT chk_conversation_messages_sender CHECK (sender IN ('USER', 'ASSISTANT'))
);

CREATE INDEX idx_conversation_messages_session ON conversation_messages(session_id, created_at);

CREATE TABLE rag_documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    document_type VARCHAR(80) NOT NULL,
    onet_code VARCHAR(20),
    language VARCHAR(10) NOT NULL DEFAULT 'vi',
    region VARCHAR(120),
    source_version VARCHAR(80),
    source_url VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_rag_documents PRIMARY KEY (id),
    CONSTRAINT fk_rag_documents_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code),
    CONSTRAINT chk_rag_documents_type CHECK (document_type IN ('OCCUPATION_OVERVIEW', 'OCCUPATION_SKILLS', 'OCCUPATION_TASKS', 'MARKET_REPORT', 'COURSE_DESCRIPTION', 'LEARNING_PATH', 'FAQ'))
);

CREATE INDEX idx_rag_documents_filters ON rag_documents(onet_code, language, region, document_type, active);

CREATE TABLE rag_retrieval_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36),
    query_text TEXT NOT NULL,
    filters_payload TEXT,
    results_payload TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_rag_retrieval_logs PRIMARY KEY (id),
    CONSTRAINT fk_rag_retrieval_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE llm_calls (
    id CHAR(36) NOT NULL,
    purpose VARCHAR(80) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(120) NOT NULL,
    latency_ms BIGINT NOT NULL,
    input_tokens INT,
    output_tokens INT,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    input_payload TEXT NOT NULL,
    output_payload TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_llm_calls PRIMARY KEY (id),
    CONSTRAINT chk_llm_calls_purpose CHECK (purpose IN ('PROFILE_EXTRACTION', 'OPEN_RESPONSE_ANALYSIS', 'RECOMMENDATION_EXPLANATION', 'CAREER_COMPARISON', 'LEARNING_PATH_GENERATION', 'CHAT_RESPONSE', 'TRANSLATION'))
);

CREATE INDEX idx_llm_calls_purpose_created ON llm_calls(purpose, created_at);

INSERT INTO rag_documents (title, content, document_type, onet_code, language, region, source_version, active, created_at, updated_at) VALUES
('Data Scientist overview', 'Nhà khoa học dữ liệu phân tích dữ liệu, xây dựng mô hình và truyền đạt insight. Đây là một lựa chọn phù hợp với người thích phân tích, học hỏi và tư duy logic.', 'OCCUPATION_OVERVIEW', '15-2051.00', 'vi', NULL, 'demo-v1', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Software Developer overview', 'Kỹ sư phần mềm thiết kế và xây dựng sản phẩm số. Nghề này yêu cầu tư duy hệ thống, kỹ năng lập trình và khả năng học công nghệ mới.', 'OCCUPATION_OVERVIEW', '15-1252.00', 'vi', NULL, 'demo-v1', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Career Compass FAQ', 'Career Compass luôn đưa ra nhiều lựa chọn nghề nghiệp và không dùng giới tính hoặc quê quán để suy luận năng lực nghề nghiệp.', 'FAQ', NULL, 'vi', NULL, 'demo-v1', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
