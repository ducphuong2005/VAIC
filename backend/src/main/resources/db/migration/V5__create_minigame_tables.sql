CREATE TABLE mini_games (
    id CHAR(36) NOT NULL,
    code VARCHAR(80) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    game_type VARCHAR(50) NOT NULL,
    estimated_minutes INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_mini_games PRIMARY KEY (id),
    CONSTRAINT uk_mini_games_code UNIQUE (code),
    CONSTRAINT chk_mini_games_type CHECK (game_type IN ('DATA_DETECTIVE', 'LOGIC_LAB', 'TEAM_QUEST'))
);

CREATE INDEX idx_mini_games_active ON mini_games(active);

CREATE TABLE mini_game_metrics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    mini_game_id CHAR(36) NOT NULL,
    metric_code VARCHAR(80) NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    min_value DECIMAL(10,2) NOT NULL DEFAULT 0,
    max_value DECIMAL(10,2) NOT NULL DEFAULT 100,
    higher_is_better BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_mini_game_metrics PRIMARY KEY (id),
    CONSTRAINT fk_mini_game_metrics_game FOREIGN KEY (mini_game_id) REFERENCES mini_games(id),
    CONSTRAINT uk_mini_game_metrics UNIQUE (mini_game_id, metric_code)
);

CREATE TABLE mini_game_onet_mapping (
    id BIGINT NOT NULL AUTO_INCREMENT,
    mini_game_metric_id BIGINT NOT NULL,
    element_id VARCHAR(50) NOT NULL,
    weight DECIMAL(5,2) NOT NULL DEFAULT 1,
    CONSTRAINT pk_mini_game_onet_mapping PRIMARY KEY (id),
    CONSTRAINT fk_mini_game_onet_mapping_metric FOREIGN KEY (mini_game_metric_id) REFERENCES mini_game_metrics(id),
    CONSTRAINT fk_mini_game_onet_mapping_element FOREIGN KEY (element_id) REFERENCES onet_elements(element_id)
);

CREATE INDEX idx_mini_game_mapping_metric ON mini_game_onet_mapping(mini_game_metric_id);

CREATE TABLE mini_game_sessions (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    mini_game_id CHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6),
    raw_metrics_payload TEXT,
    normalized_metrics_payload TEXT,
    result_summary TEXT,
    CONSTRAINT pk_mini_game_sessions PRIMARY KEY (id),
    CONSTRAINT fk_mini_game_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_mini_game_sessions_game FOREIGN KEY (mini_game_id) REFERENCES mini_games(id),
    CONSTRAINT chk_mini_game_sessions_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED'))
);

CREATE INDEX idx_mini_game_sessions_user ON mini_game_sessions(user_id, started_at);

CREATE TABLE mini_game_actions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id CHAR(36) NOT NULL,
    action_type VARCHAR(80) NOT NULL,
    action_payload TEXT NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_mini_game_actions PRIMARY KEY (id),
    CONSTRAINT fk_mini_game_actions_session FOREIGN KEY (session_id) REFERENCES mini_game_sessions(id)
);

CREATE INDEX idx_mini_game_actions_session ON mini_game_actions(session_id);

CREATE TABLE mini_game_metric_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id CHAR(36) NOT NULL,
    metric_code VARCHAR(80) NOT NULL,
    raw_value DECIMAL(10,2) NOT NULL,
    normalized_score DECIMAL(5,2) NOT NULL,
    CONSTRAINT pk_mini_game_metric_results PRIMARY KEY (id),
    CONSTRAINT fk_mini_game_metric_results_session FOREIGN KEY (session_id) REFERENCES mini_game_sessions(id),
    CONSTRAINT uk_mini_game_metric_results UNIQUE (session_id, metric_code)
);

CREATE INDEX idx_mini_game_metric_results_session ON mini_game_metric_results(session_id);

INSERT INTO mini_games (id, code, title, description, game_type, estimated_minutes, active, created_at, updated_at) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'data-detective', 'Data Detective', 'Đọc biểu đồ, phát hiện mẫu và đưa ra kết luận từ dữ liệu.', 'DATA_DETECTIVE', 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'logic-lab', 'Logic Lab', 'Giải các câu đố logic để đánh giá suy luận và kiên trì.', 'LOGIC_LAB', 9, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'team-quest', 'Team Quest', 'Xử lý tình huống nhóm theo rubric giao tiếp và hợp tác.', 'TEAM_QUEST', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO mini_game_metrics (id, mini_game_id, metric_code, metric_name, min_value, max_value, higher_is_better) VALUES
(1, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'accuracy', 'Accuracy', 0, 1, TRUE),
(2, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'durationSeconds', 'Completion Duration', 60, 900, FALSE),
(3, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'answerChanges', 'Answer Changes', 0, 10, FALSE),
(4, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'accuracy', 'Accuracy', 0, 1, TRUE),
(5, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'retryCount', 'Retry Count', 0, 10, FALSE),
(6, 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'communication', 'Communication', 0, 100, TRUE),
(7, 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'collaboration', 'Collaboration', 0, 100, TRUE);

INSERT INTO mini_game_onet_mapping (mini_game_metric_id, element_id, weight) VALUES
(1, '2.B.1.a', 1.00),
(2, '1.C.4.b', 0.60),
(3, '1.C.4.b', 0.50),
(4, '2.A.1.a', 1.00),
(5, '2.B.1.b', 0.70),
(6, '2.B.1.b', 0.80),
(7, '1.C.5.a', 0.70);
