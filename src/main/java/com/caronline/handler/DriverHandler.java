package com.caronline.handler;

import com.caronline.db.DriverRepository;
import com.caronline.http.Http;
import com.caronline.http.Json;
import com.caronline.model.Driver;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 司机接口。
 * <ul>
 *   <li>GET /api/drivers —— 列表（含绑定的车）</li>
 *   <li>POST /api/drivers —— 注册司机并同时绑定一辆车</li>
 * </ul>
 */
public class DriverHandler implements HttpHandler {

    private final DriverRepository repository = new DriverRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Http.isPreflight(exchange)) {
            return;
        }
        String path = exchange.getRequestURI().getPath();
        if (!"/api/drivers".equals(path) && !"/api/drivers/".equals(path)) {
            Http.json(exchange, 404, Json.fail("接口不存在"));
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
            List<Driver> drivers = repository.findAll();
            StringBuilder array = new StringBuilder("[");
            for (int i = 0; i < drivers.size(); i++) {
                if (i > 0) {
                    array.append(',');
                }
                array.append(drivers.get(i).toJson());
            }
            array.append(']');
            Http.json(exchange, 200, Json.ok(array.toString()));
        } catch (SQLException e) {
            e.printStackTrace();
            Http.json(exchange, 500, Json.fail("查询司机失败"));
        }
    }

    private void create(HttpExchange exchange) throws IOException {
        String body = Http.readBody(exchange);
        String name = Json.readString(body, "name");
        String phone = Json.readString(body, "phone");
        String plate = Json.readString(body, "plate");
        String brand = defaultText(Json.readString(body, "brand"));
        String color = defaultText(Json.readString(body, "color"));

        if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
            Http.json(exchange, 400, Json.fail("name 和 phone 不能为空"));
            return;
        }
        if (plate == null || plate.isBlank()) {
            Http.json(exchange, 400, Json.fail("plate（车牌）不能为空，JSON 示例：{\"name\":\"李四\",\"phone\":\"13900000000\",\"plate\":\"粤A12345\",\"brand\":\"比亚迪\",\"color\":\"白\"}"));
            return;
        }

        try {
            Driver driver = repository.insert(name.trim(), phone.trim(), plate.trim(), brand, color);
            Http.json(exchange, 200, Json.ok(driver.toJson()));
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                Http.json(exchange, 400, Json.fail("手机号或车牌已被占用"));
                return;
            }
            Http.json(exchange, 500, Json.fail("保存司机失败"));
        }
    }

    private static String defaultText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }
}
