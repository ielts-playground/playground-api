CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY user_fk (user_id) REFERENCES user (id),
    FOREIGN KEY role_fk (role_id) REFERENCES role (id)
);