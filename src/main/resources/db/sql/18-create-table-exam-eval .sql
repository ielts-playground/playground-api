CREATE TABLE IF NOT EXISTS exam_eval (
id BIGINT AUTO_INCREMENT,
exam_id BIGINT,
writing_point DECIMAL NOT NULL DEFAULT 0,
listening_point DECIMAL NOT NULL DEFAULT 0,
reading_point DECIMAL NOT NULL DEFAULT 0,
created_at DATETIME NOT NULL DEFAULT NOW(),
created_by BIGINT NOT NULL,
PRIMARY KEY (id)
);