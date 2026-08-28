package com.caronline.handler;

import com.caronline.db.PassengerRepository;
import com.caronline.http.Http;
import com.caronline.http.Json;
import com.caronline.model.Passenger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 乘客接口。数据写入 MySQL 的 passenger 表，重启后还在。
 */
public class PassengerHandler implements HttpHandler {

    private final PassengerRepository repository = new PassengerRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Http.isPreflight(exchange)) {
            return;
        }
        if (Http.isMethod(exchange, "GET")) {
            list(exchange);
            return;
        }
        if (Http.isMethod(exchange, "POST")) {
            create(exchange);
            return;
        }
        Http.json(exchange, 405, Json.fail("只支持 GET / POST"));
    }

    private void list(HttpExchange exchange) throws IOException {
        try {
            List<Passenger> passengers = repository.findAll();
            StringBuilder array = new StringBuilder("[");
            for (int i = 0; i < passengers.size(); i++) {
                if (i > 0) {
                    array.append(',');
                }
                array.append(passengers.get(i).toJson());
            }
            array.append(']');
            Http.json(exchange, 200, Json.ok(array.toString()));
        } catch (SQLException e) {
            e.printStackTrace();
            Http.json(exchange, 500, Json.fail("查询乘客失败"));
        }
    }

    private void create(HttpExchange exchange) throws IOException {
        String body = Http.readBody(exchange);
        String name = Json.readString(body, "name");
        String phone = Json.readString(body, "phone");

        if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
            Http.json(exchange, 400, Json.fail("name 和 phone 不能为空，JSON 示例：{\"name\":\"张三\",\"phone\":\"13800000000\"}"));
            return;
        }

        try {
            Passenger passenger = repository.insert(name.trim(), phone.trim());
            Http.json(exchange, 200, Json.ok(passenger.toJson()));
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                Http.json(exchange, 400, Json.fail("手机号已存在"));
                return;
            }
            Http.json(exchange, 500, Json.fail("保存乘客失败"));
        }
    }
}
