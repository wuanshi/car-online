package com.caronline.handler;

import com.caronline.db.Db;
import com.caronline.http.Http;
import com.caronline.http.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * GET /api/health —— 确认 HTTP 和 MySQL 都通。
 */
public class HealthHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!Http.isMethod(exchange, "GET")) {
            Http.json(exchange, 405, Json.fail("只支持 GET"));
            return;
        }
        String db = pingDb() ? "UP" : "DOWN";
        String data = "{\"status\":\"UP\",\"app\":\"car-online\",\"db\":" + Json.quote(db) + "}";
        Http.json(exchange, 200, Json.ok(data));
    }

    private static boolean pingDb() {
        try (Connection conn = Db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
