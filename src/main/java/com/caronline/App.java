package com.caronline;

import com.caronline.db.Db;
import com.caronline.handler.DriverHandler;
import com.caronline.handler.HealthHandler;
import com.caronline.handler.OrderHandler;
import com.caronline.handler.PassengerHandler;
import com.caronline.handler.PaymentHandler;
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
        server.createContext("/api/orders", new OrderHandler());
        server.createContext("/api/payments", new PaymentHandler());
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/favicon.ico".equals(path)) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            String data = "["
                    + "{\"method\":\"GET\",\"path\":\"/api/health\"},"
                    + "{\"method\":\"GET\",\"path\":\"/api/passengers\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/passengers\"},"
                    + "{\"method\":\"GET\",\"path\":\"/api/drivers\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/drivers\"},"
                    + "{\"method\":\"GET\",\"path\":\"/api/orders\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/orders\"},"
                    + "{\"method\":\"GET\",\"path\":\"/api/orders/{id}\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/orders/{id}/cancel\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/orders/{id}/accept\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/orders/{id}/arrive\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/orders/{id}/start\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/orders/{id}/finish\"},"
                    + "{\"method\":\"GET\",\"path\":\"/api/orders/{id}/fare\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/orders/{id}/pay\"},"
                    + "{\"method\":\"POST\",\"path\":\"/api/orders/{id}/rating\"},"
                    + "{\"method\":\"GET\",\"path\":\"/api/payments\"}"
                    + "]";
            Http.json(exchange, 200, Json.ok(data));
        });

        server.start();
        System.out.println("服务已启动（打车完整流程：发单 → 接单 → 行程 → 支付 → 评价）");
        System.out.println("接口列表  http://localhost:" + PORT + "/");
        System.out.println("按 Ctrl+C 停止");
    }
}
