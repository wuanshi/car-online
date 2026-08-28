-- 应用启动时也会执行等价的 CREATE TABLE IF NOT EXISTS。
-- 本文件方便你在 MySQL 客户端里对照表结构。

CREATE DATABASE IF NOT EXISTS car_online
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE car_online;

CREATE TABLE IF NOT EXISTS passenger (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(50)  NOT NULL,
    phone      VARCHAR(20)  NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
