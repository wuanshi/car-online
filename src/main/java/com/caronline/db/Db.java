package com.caronline.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * 数据库入口：读 db.properties，用 JDBC 拿 Connection。
 * 后面所有 SQL 都通过 {@link #getConnection()} 拿到连接再执行。
 */
public final class Db {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Db.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("找不到 classpath 上的 db.properties");
            }
            PROPS.load(in);
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Db() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("jdbc.url"),
                PROPS.getProperty("jdbc.username"),
                PROPS.getProperty("jdbc.password")
        );
    }

    /**
     * 启动时调用：确认能连上，并建好当前用到的表。
     */
    public static void init() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS passenger (
                        id         INT PRIMARY KEY AUTO_INCREMENT,
                        name       VARCHAR(50)  NOT NULL,
                        phone      VARCHAR(20)  NOT NULL UNIQUE,
                        created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS driver (
                        id         INT PRIMARY KEY AUTO_INCREMENT,
                        name       VARCHAR(50)  NOT NULL,
                        phone      VARCHAR(20)  NOT NULL UNIQUE,
                        created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS vehicle (
                        id         INT PRIMARY KEY AUTO_INCREMENT,
                        driver_id  INT          NOT NULL UNIQUE,
                        plate      VARCHAR(20)  NOT NULL UNIQUE,
                        brand      VARCHAR(50)  NOT NULL DEFAULT '',
                        color      VARCHAR(20)  NOT NULL DEFAULT '',
                        CONSTRAINT fk_vehicle_driver
                            FOREIGN KEY (driver_id) REFERENCES driver(id)
                    )
                    """);
            stmt.execute("""
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
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS payment (
                        id         INT PRIMARY KEY AUTO_INCREMENT,
                        order_id   INT           NOT NULL UNIQUE,
                        amount     DECIMAL(10,2) NOT NULL,
                        created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES ride_order(id)
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS rating (
                        id         INT PRIMARY KEY AUTO_INCREMENT,
                        order_id   INT          NOT NULL UNIQUE,
                        stars      INT          NOT NULL,
                        comment    VARCHAR(200) NOT NULL DEFAULT '',
                        created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_rating_order FOREIGN KEY (order_id) REFERENCES ride_order(id)
                    )
                    """);
        }
    }
}
