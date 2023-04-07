CREATE TABLE IF NOT EXISTS component (
    id BIGINT AUTO_INCREMENT,
    part_id BIGINT NOT NULL,
    position VARCHAR(255),
    type VARCHAR(255) NOT NULL,
    kei VARCHAR(255),
    value LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    size VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    options VARCHAR(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    PRIMARY KEY (id)
);