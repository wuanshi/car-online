package com.caronline.handler;

import com.caronline.http.Http;
import com.caronline.http.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * 练习中的用户接口，尚未完成，暂未在 App 里注册。
 */
public class User implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Http.isMethod(exchange, "POST")) {
            Http.json(exchange, 501, Json.fail("用户接口尚未实现"));
            return;
        }
        Http.json(exchange, 405, Json.fail("只支持 POST"));
    }
}
