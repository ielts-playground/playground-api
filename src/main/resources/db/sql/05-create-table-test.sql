CREATE TABLE IF NOT EXISTS test (
    id BIGINT AUTO_INCREMENT,
    description VARCHAR(1024),
    created_by BIGINT NOT NULL,
    created_at DATETIME,
    PRIMARY KEY (id)
);