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

CREATE TABLE IF NOT EXISTS driver (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(50)  NOT NULL,
    phone      VARCHAR(20)  NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicle (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    driver_id  INT          NOT NULL UNIQUE,
    plate      VARCHAR(20)  NOT NULL UNIQUE,
    brand      VARCHAR(50)  NOT NULL DEFAULT '',
    color      VARCHAR(20)  NOT NULL DEFAULT '',
    CONSTRAINT fk_vehicle_driver
        FOREIGN KEY (driver_id) REFERENCES driver(id)
);

CREATE TABLE IF NOT EXISTS ride_order (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    passenger_id  INT           NOT NULL,
    driver_id     INT           NULL,
    origin        VARCHAR(100)  NOT NULL,
    destination   VARCHAR(100)  NOT NULL,
    distance_km   DECIMAL(10,2) NOT NULL,
    status        VARCHAR(32)   NOT NULL,
    fare          DECIMAL(10,2) NULL,
    paid          TINYINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_passenger FOREIGN KEY (passenger_id) REFERENCES passenger(id),
    CONSTRAINT fk_order_driver FOREIGN KEY (driver_id) REFERENCES driver(id)
);

CREATE TABLE IF NOT EXISTS payment (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    order_id   INT           NOT NULL UNIQUE,
    amount     DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES ride_order(id)
);

CREATE TABLE IF NOT EXISTS rating (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    order_id   INT          NOT NULL UNIQUE,
    stars      INT          NOT NULL,
    comment    VARCHAR(200) NOT NULL DEFAULT '',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rating_order FOREIGN KEY (order_id) REFERENCES ride_order(id)
);
