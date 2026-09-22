-- ============================================================
-- Reference schema (for MySQL).
-- The application creates these tables automatically with
-- spring.jpa.hibernate.ddl-auto=update, so you normally do NOT
-- need to run this file yourself.
-- ============================================================

CREATE DATABASE IF NOT EXISTS taskdb;
USE taskdb;

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(120) NOT NULL,
    email      VARCHAR(120) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,   -- BCrypt hash (60 chars)
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER'
);

CREATE TABLE IF NOT EXISTS tasks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    completed   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    owner_id    BIGINT       NOT NULL,
    CONSTRAINT fk_task_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

-- Useful for lookups: each user only queries their own tasks by owner_id
CREATE INDEX idx_tasks_owner ON tasks (owner_id);