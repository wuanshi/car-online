package com.caronline.handler;

import com.caronline.common.BizException;
import com.caronline.http.Http;
import com.caronline.http.Json;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 把业务结果写成统一 JSON：BizException → 4xx，SQLException → 500。
 */
final class Handlers {

    @FunctionalInterface
    interface Action {
        String apply() throws SQLException;
    }

    private Handlers() {
    }

    static void run(HttpExchange exchange, Action action) throws IOException {
        try {
            Http.json(exchange, 200, Json.ok(action.apply()));
        } catch (BizException e) {
            Http.json(exchange, e.getHttpStatus(), Json.fail(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            Http.json(exchange, 500, Json.fail("数据库操作失败"));
        }
    }
}
