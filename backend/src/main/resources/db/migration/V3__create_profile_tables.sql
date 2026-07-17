CREATE TABLE student_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    education_level VARCHAR(50),
    school VARCHAR(255),
    current_major VARCHAR(255),
    preferred_regions VARCHAR(500),
    learning_budget DECIMAL(12,2),
    available_learning_hours_per_week INT,
    preferred_education_routes VARCHAR(500),
    career_goals TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_student_profiles PRIMARY KEY (id),
    CONSTRAINT uk_student_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_student_profiles_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_existing_skills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    skill_name VARCHAR(190) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_user_existing_skills PRIMARY KEY (id),
    CONSTRAINT fk_user_existing_skills_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_user_existing_skills UNIQUE (user_id, skill_name)
);

CREATE INDEX idx_user_existing_skills_user_id ON user_existing_skills(user_id);

CREATE TABLE user_career_preferences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    onet_code VARCHAR(20),
    preference_score DECIMAL(5,2) NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_user_career_preferences PRIMARY KEY (id),
    CONSTRAINT fk_user_career_preferences_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_career_preferences_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code)
);

CREATE INDEX idx_user_career_preferences_user_id ON user_career_preferences(user_id);

CREATE TABLE user_profile_dimensions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    element_id VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    confidence DECIMAL(5,2) NOT NULL,
    evidence_count INT NOT NULL,
    last_calculated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_user_profile_dimensions PRIMARY KEY (id),
    CONSTRAINT fk_user_profile_dimensions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_profile_dimensions_element FOREIGN KEY (element_id) REFERENCES onet_elements(element_id),
    CONSTRAINT uk_user_profile_dimensions UNIQUE (user_id, element_id)
);

CREATE INDEX idx_user_profile_dimensions_user_id ON user_profile_dimensions(user_id);

CREATE TABLE profile_evidence (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id VARCHAR(80),
    element_id VARCHAR(50) NOT NULL,
    evidence_score DECIMAL(5,2) NOT NULL,
    evidence_confidence DECIMAL(5,2) NOT NULL,
    evidence_payload TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_profile_evidence PRIMARY KEY (id),
    CONSTRAINT fk_profile_evidence_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_profile_evidence_element FOREIGN KEY (element_id) REFERENCES onet_elements(element_id),
    CONSTRAINT chk_profile_evidence_source CHECK (source_type IN ('ASSESSMENT', 'MINI_GAME', 'LLM_SUGGESTION', 'MANUAL'))
);

CREATE INDEX idx_profile_evidence_user_element ON profile_evidence(user_id, element_id);
