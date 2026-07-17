CREATE TABLE majors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    education_route VARCHAR(50) NOT NULL,
    description TEXT,
    CONSTRAINT pk_majors PRIMARY KEY (id)
);

CREATE TABLE occupation_majors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    onet_code VARCHAR(20) NOT NULL,
    major_id BIGINT NOT NULL,
    CONSTRAINT pk_occupation_majors PRIMARY KEY (id),
    CONSTRAINT fk_occupation_majors_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code),
    CONSTRAINT fk_occupation_majors_major FOREIGN KEY (major_id) REFERENCES majors(id)
);

CREATE TABLE training_providers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    provider_type VARCHAR(80),
    website_url VARCHAR(1000),
    CONSTRAINT pk_training_providers PRIMARY KEY (id)
);

CREATE TABLE courses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    route VARCHAR(50) NOT NULL,
    duration_hours INT,
    url VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_courses PRIMARY KEY (id),
    CONSTRAINT fk_courses_provider FOREIGN KEY (provider_id) REFERENCES training_providers(id)
);

CREATE TABLE course_skills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    skill_name VARCHAR(190) NOT NULL,
    CONSTRAINT pk_course_skills PRIMARY KEY (id),
    CONSTRAINT fk_course_skills_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE INDEX idx_course_skills_name ON course_skills(skill_name);

CREATE TABLE learning_paths (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    onet_code VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    route VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_learning_paths PRIMARY KEY (id),
    CONSTRAINT fk_learning_paths_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_learning_paths_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code)
);

CREATE INDEX idx_learning_paths_user ON learning_paths(user_id, updated_at);

CREATE TABLE learning_path_steps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    learning_path_id CHAR(36) NOT NULL,
    step_order INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    target_skill VARCHAR(190),
    course_id BIGINT,
    duration_hours INT,
    progress_percent INT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_learning_path_steps PRIMARY KEY (id),
    CONSTRAINT fk_learning_path_steps_path FOREIGN KEY (learning_path_id) REFERENCES learning_paths(id),
    CONSTRAINT fk_learning_path_steps_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE user_favorite_occupations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    onet_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_user_favorite_occupations PRIMARY KEY (id),
    CONSTRAINT uk_user_favorite_occupations UNIQUE (user_id, onet_code),
    CONSTRAINT fk_user_favorite_occupations_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_favorite_occupations_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code)
);

CREATE TABLE user_favorite_courses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    course_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_user_favorite_courses PRIMARY KEY (id),
    CONSTRAINT uk_user_favorite_courses UNIQUE (user_id, course_id),
    CONSTRAINT fk_user_favorite_courses_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_favorite_courses_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE activity_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    activity_type VARCHAR(80) NOT NULL,
    message VARCHAR(500) NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_activity_logs PRIMARY KEY (id),
    CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_activity_logs_user_created ON activity_logs(user_id, created_at);

CREATE TABLE fairness_test_runs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    status VARCHAR(50) NOT NULL,
    summary TEXT NOT NULL,
    passed BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_fairness_test_runs PRIMARY KEY (id)
);

CREATE TABLE recommendation_audits (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recommendation_id BIGINT,
    audit_type VARCHAR(80) NOT NULL,
    passed BOOLEAN NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_recommendation_audits PRIMARY KEY (id),
    CONSTRAINT fk_recommendation_audits_recommendation FOREIGN KEY (recommendation_id) REFERENCES career_recommendations(id)
);

INSERT INTO training_providers (id, name, provider_type, website_url) VALUES
(1, 'Career Compass Academy', 'ONLINE', 'https://example.com');

INSERT INTO courses (id, provider_id, title, description, route, duration_hours, url, active) VALUES
(1, 1, 'SQL for Data Analysis', 'Nền tảng SQL cho phân tích dữ liệu.', 'CERTIFICATE', 24, 'https://example.com/sql', TRUE),
(2, 1, 'Python Data Foundations', 'Python, pandas và trực quan hóa dữ liệu cơ bản.', 'SELF_STUDY', 36, 'https://example.com/python-data', TRUE),
(3, 1, 'Software Project Basics', 'Git, testing và quy trình phát triển phần mềm.', 'BOOTCAMP', 30, 'https://example.com/software-basics', TRUE);

INSERT INTO course_skills (course_id, skill_name) VALUES
(1, 'SQL'),
(2, 'Python'),
(3, 'Git'),
(3, 'JavaScript');
