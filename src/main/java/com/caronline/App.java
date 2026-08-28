package com.caronline;

import com.caronline.db.Db;
import com.caronline.handler.DriverHandler;
import com.caronline.handler.HealthHandler;
import com.caronline.handler.PassengerHandler;
import com.caronline.http.Http;
import com.caronline.http.Json;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

/**
 * 程序入口：先连 MySQL，再启动 HttpServer。
 */
public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        try {
            Db.init();
            System.out.println("MySQL 已连接，数据表已就绪");
        } catch (SQLException e) {
            System.err.println("无法连接 MySQL，请检查：");
            System.err.println("  1. MySQL 服务是否启动");
            System.err.println("  2. src/main/resources/db.properties 里的账号密码");
            e.printStackTrace();
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/passengers", new PassengerHandler());
        server.createContext("/api/drivers", new DriverHandler());
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/favicon.ico".equals(path)) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            String data = "["
                    + "{\"method\":\"GET\",\"path\":\"/api/health\",\"desc\":\"健康检查（含数据库）\"},"
                    + "{\"method\":\"GET\",\"path\":\"/api/passengers\",\"desc\":\"乘客列表\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/passengers\",\"desc\":\"创建乘客\"},"
                    + "{\"method\":\"GET\",\"path\":\"/api/drivers\",\"desc\":\"司机列表\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/drivers\",\"desc\":\"注册司机并绑车\"}"
                    + "]";
            Http.json(exchange, 200, Json.ok(data));
        });

        server.start();
        System.out.println("服务已启动（纯 Java HttpServer + JDBC）");
        System.out.println("接口列表  http://localhost:" + PORT + "/");
        System.out.println("健康检查  http://localhost:" + PORT + "/api/health");
        System.out.println("按 Ctrl+C 停止");
    }
}
