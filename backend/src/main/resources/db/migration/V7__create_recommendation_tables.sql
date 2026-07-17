CREATE TABLE recommendation_runs (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    profile_snapshot TEXT NOT NULL,
    profile_confidence DECIMAL(5,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_recommendation_runs PRIMARY KEY (id),
    CONSTRAINT fk_recommendation_runs_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_recommendation_runs_status CHECK (status IN ('COMPLETED', 'FAILED'))
);

CREATE INDEX idx_recommendation_runs_user_created ON recommendation_runs(user_id, created_at);

CREATE TABLE career_recommendations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    run_id CHAR(36) NOT NULL,
    onet_code VARCHAR(20) NOT NULL,
    recommendation_group VARCHAR(50) NOT NULL,
    rank_number INT NOT NULL,
    interest_score DECIMAL(5,2) NOT NULL,
    ability_score DECIMAL(5,2) NOT NULL,
    skill_score DECIMAL(5,2) NOT NULL,
    work_style_score DECIMAL(5,2) NOT NULL,
    market_score DECIMAL(5,2) NOT NULL,
    feasibility_score DECIMAL(5,2) NOT NULL,
    final_score DECIMAL(5,2) NOT NULL,
    confidence DECIMAL(5,2) NOT NULL,
    reasons TEXT,
    considerations TEXT,
    sources TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_career_recommendations PRIMARY KEY (id),
    CONSTRAINT fk_career_recommendations_run FOREIGN KEY (run_id) REFERENCES recommendation_runs(id),
    CONSTRAINT fk_career_recommendations_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code),
    CONSTRAINT uk_career_recommendations_rank UNIQUE (run_id, rank_number),
    CONSTRAINT uk_career_recommendations_career UNIQUE (run_id, onet_code),
    CONSTRAINT chk_career_recommendations_group CHECK (recommendation_group IN ('DIRECT_MATCH', 'GROWTH_OPTION', 'EXPLORATION_OPTION'))
);

CREATE INDEX idx_career_recommendations_run ON career_recommendations(run_id, rank_number);

CREATE TABLE recommendation_evidence (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recommendation_id BIGINT NOT NULL,
    evidence_type VARCHAR(50) NOT NULL,
    evidence_text TEXT NOT NULL,
    score DECIMAL(5,2),
    CONSTRAINT pk_recommendation_evidence PRIMARY KEY (id),
    CONSTRAINT fk_recommendation_evidence_recommendation FOREIGN KEY (recommendation_id) REFERENCES career_recommendations(id)
);

CREATE INDEX idx_recommendation_evidence_recommendation ON recommendation_evidence(recommendation_id);

CREATE TABLE skill_gaps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recommendation_id BIGINT NOT NULL,
    skill_name VARCHAR(190) NOT NULL,
    current_score DECIMAL(5,2) NOT NULL,
    required_score DECIMAL(5,2) NOT NULL,
    gap_score DECIMAL(5,2) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    suggested_courses TEXT,
    CONSTRAINT pk_skill_gaps PRIMARY KEY (id),
    CONSTRAINT fk_skill_gaps_recommendation FOREIGN KEY (recommendation_id) REFERENCES career_recommendations(id),
    CONSTRAINT chk_skill_gaps_priority CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW'))
);

CREATE INDEX idx_skill_gaps_recommendation ON skill_gaps(recommendation_id);

CREATE TABLE recommendation_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recommendation_id BIGINT NOT NULL,
    user_id CHAR(36) NOT NULL,
    rating INT,
    feedback_text TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_recommendation_feedback PRIMARY KEY (id),
    CONSTRAINT fk_recommendation_feedback_recommendation FOREIGN KEY (recommendation_id) REFERENCES career_recommendations(id),
    CONSTRAINT fk_recommendation_feedback_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_recommendation_feedback_rating CHECK (rating IS NULL OR (rating >= 1 AND rating <= 5))
);

CREATE INDEX idx_recommendation_feedback_recommendation ON recommendation_feedback(recommendation_id);
