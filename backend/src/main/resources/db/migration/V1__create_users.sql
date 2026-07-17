CREATE TABLE users (
    id CHAR(36) NOT NULL,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'COUNSELOR', 'ADMIN', 'CRAWLER')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

CREATE TABLE user_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'vi',
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_user_settings PRIMARY KEY (id),
    CONSTRAINT uk_user_settings_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
);
