CREATE TABLE user_favorite_links (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    link_type VARCHAR(40) NOT NULL,
    provider VARCHAR(120) NOT NULL,
    title VARCHAR(255) NOT NULL,
    url VARCHAR(700) NOT NULL,
    description TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_user_favorite_links PRIMARY KEY (id),
    CONSTRAINT uk_user_favorite_links UNIQUE (user_id, url),
    CONSTRAINT fk_user_favorite_links_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_favorite_links_user_type ON user_favorite_links(user_id, link_type, created_at);
