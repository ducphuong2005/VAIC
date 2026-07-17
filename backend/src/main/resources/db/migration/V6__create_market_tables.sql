CREATE TABLE crawl_runs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_name VARCHAR(80) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    finished_at TIMESTAMP(6),
    inserted_count INT NOT NULL DEFAULT 0,
    updated_count INT NOT NULL DEFAULT 0,
    duplicate_count INT NOT NULL DEFAULT 0,
    rejected_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    CONSTRAINT pk_crawl_runs PRIMARY KEY (id),
    CONSTRAINT chk_crawl_runs_status CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_crawl_runs_source_started ON crawl_runs(source_name, started_at);

CREATE TABLE job_postings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_name VARCHAR(80) NOT NULL,
    external_id VARCHAR(190) NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    location VARCHAR(255),
    region VARCHAR(120),
    salary_min DECIMAL(14,2),
    salary_max DECIMAL(14,2),
    remote BOOLEAN NOT NULL DEFAULT FALSE,
    entry_level BOOLEAN NOT NULL DEFAULT FALSE,
    onet_code VARCHAR(20),
    posted_at TIMESTAMP(6),
    crawled_at TIMESTAMP(6) NOT NULL,
    raw_url VARCHAR(1000),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_job_postings PRIMARY KEY (id),
    CONSTRAINT uk_job_postings_source_external UNIQUE (source_name, external_id),
    CONSTRAINT uk_job_postings_content_hash UNIQUE (content_hash),
    CONSTRAINT fk_job_postings_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code)
);

CREATE INDEX idx_job_postings_onet_region ON job_postings(onet_code, region);
CREATE INDEX idx_job_postings_region ON job_postings(region);
CREATE INDEX idx_job_postings_source ON job_postings(source_name);

CREATE TABLE job_posting_skills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    job_posting_id BIGINT NOT NULL,
    skill_name VARCHAR(190) NOT NULL,
    normalized_skill_name VARCHAR(190) NOT NULL,
    CONSTRAINT pk_job_posting_skills PRIMARY KEY (id),
    CONSTRAINT fk_job_posting_skills_job FOREIGN KEY (job_posting_id) REFERENCES job_postings(id),
    CONSTRAINT uk_job_posting_skills UNIQUE (job_posting_id, normalized_skill_name)
);

CREATE INDEX idx_job_posting_skills_name ON job_posting_skills(normalized_skill_name);

CREATE TABLE market_signals (
    id BIGINT NOT NULL AUTO_INCREMENT,
    onet_code VARCHAR(20) NOT NULL,
    region VARCHAR(120) NOT NULL,
    source VARCHAR(80) NOT NULL,
    period_label VARCHAR(50) NOT NULL,
    job_count INT NOT NULL,
    growth_rate DECIMAL(8,2),
    median_salary DECIMAL(14,2),
    entry_level_ratio DECIMAL(5,2),
    remote_ratio DECIMAL(5,2),
    demand_score DECIMAL(5,2) NOT NULL,
    data_confidence DECIMAL(5,2) NOT NULL,
    sample_size INT NOT NULL,
    last_updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_market_signals PRIMARY KEY (id),
    CONSTRAINT fk_market_signals_occupation FOREIGN KEY (onet_code) REFERENCES occupations(onet_code),
    CONSTRAINT uk_market_signals UNIQUE (onet_code, region, source, period_label)
);

CREATE INDEX idx_market_signals_onet_region ON market_signals(onet_code, region);
CREATE INDEX idx_market_signals_demand ON market_signals(demand_score);

CREATE TABLE job_postings_raw (
    id BIGINT NOT NULL AUTO_INCREMENT,
    crawl_run_id BIGINT,
    source_name VARCHAR(80) NOT NULL,
    external_id VARCHAR(190),
    content_hash VARCHAR(128),
    raw_payload LONGTEXT NOT NULL,
    accepted BOOLEAN NOT NULL DEFAULT FALSE,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_job_postings_raw PRIMARY KEY (id),
    CONSTRAINT fk_job_postings_raw_crawl_run FOREIGN KEY (crawl_run_id) REFERENCES crawl_runs(id)
);

CREATE INDEX idx_job_postings_raw_run ON job_postings_raw(crawl_run_id);
