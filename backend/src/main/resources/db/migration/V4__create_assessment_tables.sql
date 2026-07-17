CREATE TABLE assessments (
    id CHAR(36) NOT NULL,
    code VARCHAR(80) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assessment_type VARCHAR(50) NOT NULL,
    estimated_minutes INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_assessments PRIMARY KEY (id),
    CONSTRAINT uk_assessments_code UNIQUE (code),
    CONSTRAINT chk_assessments_type CHECK (assessment_type IN ('PERSONALITY_DISCOVERY', 'CAREER_INTERESTS', 'ABILITY_ASSESSMENT', 'CAREER_VALUES'))
);

CREATE INDEX idx_assessments_active ON assessments(active);

CREATE TABLE assessment_questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    assessment_id CHAR(36) NOT NULL,
    question_text TEXT NOT NULL,
    display_order INT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    CONSTRAINT pk_assessment_questions PRIMARY KEY (id),
    CONSTRAINT fk_assessment_questions_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(id),
    CONSTRAINT chk_assessment_questions_type CHECK (question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'LIKERT'))
);

CREATE INDEX idx_assessment_questions_assessment ON assessment_questions(assessment_id, display_order);

CREATE TABLE assessment_options (
    id BIGINT NOT NULL AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    display_order INT NOT NULL,
    scoring_payload TEXT NOT NULL,
    CONSTRAINT pk_assessment_options PRIMARY KEY (id),
    CONSTRAINT fk_assessment_options_question FOREIGN KEY (question_id) REFERENCES assessment_questions(id)
);

CREATE INDEX idx_assessment_options_question ON assessment_options(question_id, display_order);

CREATE TABLE assessment_sessions (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    assessment_id CHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6),
    raw_score_payload TEXT,
    normalized_score_payload TEXT,
    CONSTRAINT pk_assessment_sessions PRIMARY KEY (id),
    CONSTRAINT fk_assessment_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_assessment_sessions_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(id),
    CONSTRAINT chk_assessment_sessions_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED'))
);

CREATE INDEX idx_assessment_sessions_user ON assessment_sessions(user_id, started_at);

CREATE TABLE assessment_answers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id CHAR(36) NOT NULL,
    question_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    answered_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_assessment_answers PRIMARY KEY (id),
    CONSTRAINT fk_assessment_answers_session FOREIGN KEY (session_id) REFERENCES assessment_sessions(id),
    CONSTRAINT fk_assessment_answers_question FOREIGN KEY (question_id) REFERENCES assessment_questions(id),
    CONSTRAINT fk_assessment_answers_option FOREIGN KEY (option_id) REFERENCES assessment_options(id),
    CONSTRAINT uk_assessment_answers UNIQUE (session_id, question_id, option_id)
);

CREATE INDEX idx_assessment_answers_session ON assessment_answers(session_id);

INSERT INTO assessments (id, code, title, description, assessment_type, estimated_minutes, active, created_at, updated_at) VALUES
('11111111-1111-1111-1111-111111111111', 'personality-discovery', 'Personality Discovery', 'Khám phá phong cách làm việc và cách ra quyết định.', 'PERSONALITY_DISCOVERY', 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'career-interests', 'Career Interests', 'Xác định nhóm hoạt động nghề nghiệp tạo hứng thú.', 'CAREER_INTERESTS', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'ability-assessment', 'Ability Assessment', 'Đánh giá năng lực logic, phân tích và học tập.', 'ABILITY_ASSESSMENT', 12, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444444', 'career-values', 'Career Values', 'Làm rõ giá trị công việc và môi trường phù hợp.', 'CAREER_VALUES', 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_questions (id, assessment_id, question_text, display_order, question_type) VALUES
(1, '22222222-2222-2222-2222-222222222222', 'Bạn thích hoạt động nào nhất khi giải quyết một vấn đề mới?', 1, 'SINGLE_CHOICE'),
(2, '22222222-2222-2222-2222-222222222222', 'Môi trường nào khiến bạn dễ tập trung nhất?', 2, 'SINGLE_CHOICE'),
(3, '33333333-3333-3333-3333-333333333333', 'Khi gặp dữ liệu lạ, bạn thường làm gì trước?', 1, 'SINGLE_CHOICE');

INSERT INTO assessment_options (id, question_id, option_text, display_order, scoring_payload) VALUES
(1, 1, 'Tìm mẫu, đặt giả thuyết và kiểm chứng bằng dữ liệu.', 1, '{"RIASEC-I":8,"2.B.1.a":6}'),
(2, 1, 'Sắp xếp thông tin thành bảng và quy trình rõ ràng.', 2, '{"RIASEC-C":7,"1.C.4.b":6}'),
(3, 2, 'Không gian yên tĩnh để phân tích sâu.', 1, '{"RIASEC-I":6,"1.C.4.b":5}'),
(4, 2, 'Nhóm nhỏ cùng trao đổi ý tưởng mới.', 2, '{"1.C.5.a":7,"2.B.1.b":5}'),
(5, 3, 'Làm sạch dữ liệu, kiểm tra ngoại lệ rồi phân tích.', 1, '{"2.A.1.e":8,"2.B.1.a":7,"1.C.4.b":6}'),
(6, 3, 'Thử nhanh nhiều hướng và học từ phản hồi.', 2, '{"2.B.1.b":8,"1.C.5.a":6}');
