CREATE TABLE occupations (
    onet_code VARCHAR(20) NOT NULL,
    title_vi VARCHAR(255) NOT NULL,
    title_en VARCHAR(255) NOT NULL,
    description TEXT,
    career_cluster VARCHAR(120),
    job_zone INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_occupations PRIMARY KEY (onet_code)
);

CREATE INDEX idx_occupations_active ON occupations(active);
CREATE INDEX idx_occupations_cluster ON occupations(career_cluster);

CREATE TABLE occupation_aliases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    onet_code VARCHAR(20) NOT NULL,
    alias VARCHAR(255) NOT NULL,
    CONSTRAINT pk_occupation_aliases PRIMARY KEY (id),
    CONSTRAINT fk_occupation_aliases_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code)
);

CREATE INDEX idx_occupation_aliases_alias ON occupation_aliases(alias);

CREATE TABLE onet_elements (
    element_id VARCHAR(50) NOT NULL,
    element_name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    CONSTRAINT pk_onet_elements PRIMARY KEY (element_id),
    CONSTRAINT chk_onet_elements_category CHECK (category IN ('INTEREST', 'ABILITY', 'SKILL', 'WORK_STYLE', 'KNOWLEDGE'))
);

CREATE INDEX idx_onet_elements_category ON onet_elements(category);

CREATE TABLE occupation_element_scores (
    id BIGINT NOT NULL AUTO_INCREMENT,
    onet_code VARCHAR(20) NOT NULL,
    element_id VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    CONSTRAINT pk_occupation_element_scores PRIMARY KEY (id),
    CONSTRAINT fk_occupation_element_scores_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code),
    CONSTRAINT fk_occupation_element_scores_element FOREIGN KEY (element_id) REFERENCES onet_elements(element_id),
    CONSTRAINT uk_occupation_element_scores UNIQUE (onet_code, element_id),
    CONSTRAINT chk_occupation_element_scores_category CHECK (category IN ('INTEREST', 'ABILITY', 'SKILL', 'WORK_STYLE', 'KNOWLEDGE'))
);

CREATE INDEX idx_occupation_element_scores_lookup ON occupation_element_scores(onet_code, category, score);

CREATE TABLE occupation_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    onet_code VARCHAR(20) NOT NULL,
    task_text TEXT NOT NULL,
    importance_score DECIMAL(5,2),
    CONSTRAINT pk_occupation_tasks PRIMARY KEY (id),
    CONSTRAINT fk_occupation_tasks_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code)
);

CREATE INDEX idx_occupation_tasks_onet_code ON occupation_tasks(onet_code);

CREATE TABLE related_occupations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    onet_code VARCHAR(20) NOT NULL,
    related_onet_code VARCHAR(20) NOT NULL,
    relation_type VARCHAR(50) NOT NULL,
    CONSTRAINT pk_related_occupations PRIMARY KEY (id),
    CONSTRAINT fk_related_occupations_source FOREIGN KEY (onet_code) REFERENCES occupations(onet_code),
    CONSTRAINT fk_related_occupations_target FOREIGN KEY (related_onet_code) REFERENCES occupations(onet_code),
    CONSTRAINT uk_related_occupations UNIQUE (onet_code, related_onet_code)
);

CREATE INDEX idx_related_occupations_onet_code ON related_occupations(onet_code);

CREATE TABLE technology_skills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    skill_name VARCHAR(190) NOT NULL,
    category VARCHAR(120),
    CONSTRAINT pk_technology_skills PRIMARY KEY (id),
    CONSTRAINT uk_technology_skills_name UNIQUE (skill_name)
);

CREATE TABLE occupation_technology_skills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    onet_code VARCHAR(20) NOT NULL,
    technology_skill_id BIGINT NOT NULL,
    required_score DECIMAL(5,2),
    CONSTRAINT pk_occupation_technology_skills PRIMARY KEY (id),
    CONSTRAINT fk_occupation_technology_skills_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code),
    CONSTRAINT fk_occupation_technology_skills_skill FOREIGN KEY (technology_skill_id) REFERENCES technology_skills(id),
    CONSTRAINT uk_occupation_technology_skills UNIQUE (onet_code, technology_skill_id)
);

CREATE INDEX idx_occupation_technology_skills_onet_code ON occupation_technology_skills(onet_code);

INSERT INTO occupations (onet_code, title_vi, title_en, description, career_cluster, job_zone, active, created_at, updated_at) VALUES
('15-2051.00', 'Nhà khoa học dữ liệu', 'Data Scientist', 'Phân tích dữ liệu, xây dựng mô hình machine learning và chuyển dữ liệu thành quyết định.', 'Công nghệ thông tin', 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('15-1252.00', 'Kỹ sư phần mềm', 'Software Developer', 'Thiết kế, phát triển và bảo trì phần mềm phục vụ người dùng và doanh nghiệp.', 'Công nghệ thông tin', 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('13-1161.00', 'Chuyên viên phân tích marketing', 'Market Research Analyst', 'Nghiên cứu thị trường, hành vi khách hàng và hiệu quả chiến dịch.', 'Kinh doanh', 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO occupation_aliases (onet_code, alias) VALUES
('15-2051.00', 'Data Analyst'),
('15-2051.00', 'Machine Learning Engineer'),
('15-1252.00', 'Lập trình viên'),
('15-1252.00', 'Software Engineer'),
('13-1161.00', 'Digital Marketing Analyst'),
('13-1161.00', 'Marketing số');

INSERT INTO onet_elements (element_id, element_name, category, description) VALUES
('RIASEC-I', 'Investigative Interest', 'INTEREST', 'Thích tìm hiểu, phân tích và giải quyết vấn đề.'),
('RIASEC-C', 'Conventional Interest', 'INTEREST', 'Thích cấu trúc, dữ liệu và quy trình rõ ràng.'),
('2.A.1.a', 'Deductive Reasoning', 'ABILITY', 'Khả năng áp dụng quy tắc logic để giải quyết vấn đề.'),
('2.A.1.e', 'Mathematical Reasoning', 'ABILITY', 'Khả năng chọn phương pháp toán phù hợp.'),
('2.B.1.a', 'Critical Thinking', 'SKILL', 'Tư duy phản biện khi đánh giá giải pháp.'),
('2.B.1.b', 'Active Learning', 'SKILL', 'Học nhanh và cập nhật kiến thức mới.'),
('1.C.4.b', 'Attention to Detail', 'WORK_STYLE', 'Cẩn thận và chính xác khi làm việc.'),
('1.C.5.a', 'Innovation', 'WORK_STYLE', 'Sẵn sàng thử cách tiếp cận mới.');

INSERT INTO occupation_element_scores (onet_code, element_id, category, score) VALUES
('15-2051.00', 'RIASEC-I', 'INTEREST', 92),
('15-2051.00', 'RIASEC-C', 'INTEREST', 78),
('15-2051.00', '2.A.1.a', 'ABILITY', 88),
('15-2051.00', '2.A.1.e', 'ABILITY', 90),
('15-2051.00', '2.B.1.a', 'SKILL', 86),
('15-2051.00', '2.B.1.b', 'SKILL', 82),
('15-2051.00', '1.C.4.b', 'WORK_STYLE', 84),
('15-2051.00', '1.C.5.a', 'WORK_STYLE', 76),
('15-1252.00', 'RIASEC-I', 'INTEREST', 84),
('15-1252.00', '2.A.1.a', 'ABILITY', 86),
('15-1252.00', '2.B.1.a', 'SKILL', 88),
('15-1252.00', '1.C.5.a', 'WORK_STYLE', 80),
('13-1161.00', 'RIASEC-C', 'INTEREST', 75),
('13-1161.00', '2.B.1.a', 'SKILL', 78),
('13-1161.00', '2.B.1.b', 'SKILL', 74),
('13-1161.00', '1.C.5.a', 'WORK_STYLE', 82);

INSERT INTO occupation_tasks (onet_code, task_text, importance_score) VALUES
('15-2051.00', 'Thu thập, làm sạch và phân tích dữ liệu từ nhiều nguồn.', 90),
('15-2051.00', 'Xây dựng mô hình dự báo và trình bày insight cho stakeholder.', 88),
('15-1252.00', 'Thiết kế, viết, kiểm thử và triển khai phần mềm.', 91),
('15-1252.00', 'Phối hợp với nhóm sản phẩm để cải thiện trải nghiệm người dùng.', 78),
('13-1161.00', 'Phân tích dữ liệu thị trường và hành vi khách hàng.', 86),
('13-1161.00', 'Đánh giá hiệu quả chiến dịch marketing.', 80);

INSERT INTO related_occupations (onet_code, related_onet_code, relation_type) VALUES
('15-2051.00', '15-1252.00', 'SIMILAR_SKILLS'),
('15-2051.00', '13-1161.00', 'DATA_ORIENTED'),
('15-1252.00', '15-2051.00', 'SIMILAR_SKILLS'),
('13-1161.00', '15-2051.00', 'DATA_ORIENTED');

INSERT INTO technology_skills (id, skill_name, category) VALUES
(1, 'Python', 'Programming'),
(2, 'SQL', 'Database'),
(3, 'Machine Learning', 'AI'),
(4, 'JavaScript', 'Programming'),
(5, 'Git', 'Developer Tools'),
(6, 'SEO Analytics', 'Marketing');

INSERT INTO occupation_technology_skills (onet_code, technology_skill_id, required_score) VALUES
('15-2051.00', 1, 85),
('15-2051.00', 2, 82),
('15-2051.00', 3, 78),
('15-1252.00', 4, 82),
('15-1252.00', 5, 76),
('13-1161.00', 2, 65),
('13-1161.00', 6, 80);
