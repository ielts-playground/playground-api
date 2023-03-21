CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(1024) NOT NULL,
    email VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    PRIMARY KEY (id)
);