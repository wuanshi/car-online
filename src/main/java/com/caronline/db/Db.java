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
        }
    }
}
