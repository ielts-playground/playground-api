CREATE TABLE IF NOT EXISTS part (
    id BIGINT AUTO_INCREMENT,
    test_id BIGINT ,
    type VARCHAR(255) NOT NULL,
    number BIGINT,
    PRIMARY KEY (id)
);