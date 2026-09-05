package com.caronline.handler;

import com.caronline.common.BizException;
import com.caronline.http.Http;
import com.caronline.http.Json;
import com.caronline.http.Paths;
import com.caronline.model.Driver;
import com.caronline.service.DriverService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Optional;

public class DriverHandler implements HttpHandler {

    private final DriverService service = new DriverService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Http.isPreflight(exchange)) {
            return;
        }

        Optional<Integer> id;
        try {
            id = Paths.readId(exchange.getRequestURI().getPath(), "/api/drivers");
        } catch (IllegalArgumentException e) {
            Http.json(exchange, 400, Json.fail(e.getMessage()));
            return;
        }

        if (Http.isMethod(exchange, "GET")) {
            if (id.isPresent()) {
                Handlers.run(exchange, () -> service.findById(id.get())
                        .orElseThrow(() -> new BizException(404, "司机不存在"))
                        .toJson());
            } else {
                Handlers.run(exchange, () -> Json.array(service.list().stream().map(Driver::toJson).toList()));
            }
            return;
        }
        if (Http.isMethod(exchange, "POST") && id.isEmpty()) {
            String body = Http.readBody(exchange);
            Handlers.run(exchange, () -> service.register(
                    Json.readString(body, "name"),
                    Json.readString(body, "phone"),
                    Json.readString(body, "plate"),
                    Json.readString(body, "brand"),
                    Json.readString(body, "color")
            ).toJson());
            return;
        }
        Http.json(exchange, 405, Json.fail("只支持 GET / POST"));
    }
}
