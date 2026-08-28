package com.caronline.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 读写一次 HTTP 请求 / 响应。后面每个接口 Handler 都复用这里。
 */
public final class Http {

    private Http() {
    }

    public static void applyCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * 浏览器跨域 POST 会先发 OPTIONS。返回 true 表示已处理完，Handler 直接 return。
     */
    public static boolean isPreflight(HttpExchange exchange) throws IOException {
        applyCors(exchange);
        if (!isMethod(exchange, "OPTIONS")) {
            return false;
        }
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return true;
    }

    public static void json(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        applyCors(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static boolean isMethod(HttpExchange exchange, String method) {
        return method.equalsIgnoreCase(exchange.getRequestMethod());
    }
}
