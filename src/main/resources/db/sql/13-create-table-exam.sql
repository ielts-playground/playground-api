CREATE TABLE IF NOT EXISTS exam (
    id BIGINT AUTO_INCREMENT,
    user_id BIGINT ,
    submitted_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT NOW(),
    modified_at DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id)
);