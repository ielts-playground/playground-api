CREATE TABLE IF NOT EXISTS exam (
    id BIGINT AUTO_INCREMENT,
    user_id BIGINT ,
    created_at DATETIME,
    submitted_at DATETIME,
    PRIMARY KEY (id)
);