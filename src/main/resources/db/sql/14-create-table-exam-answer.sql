CREATE TABLE IF NOT EXISTS exam_answer (
    exam_part_id BIGINT NOT NULL,
    kei VARCHAR(255) NOT NULL,
    value TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT NOW(),
    modified_at DATETIME NOT NULL DEFAULT NOW()
);