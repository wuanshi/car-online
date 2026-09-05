package com.caronline.handler;

import com.caronline.http.Http;
import com.caronline.http.Json;
import com.caronline.http.Paths;
import com.caronline.model.Passenger;
import com.caronline.service.PassengerService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Optional;

public class PassengerHandler implements HttpHandler {

    private final PassengerService service = new PassengerService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Http.isPreflight(exchange)) {
            return;
        }

        Optional<Integer> id;
        try {
            id = Paths.readId(exchange.getRequestURI().getPath(), "/api/passengers");
        } catch (IllegalArgumentException e) {
            Http.json(exchange, 400, Json.fail(e.getMessage()));
            return;
        }

        if (Http.isMethod(exchange, "GET")) {
            if (id.isPresent()) {
                Handlers.run(exchange, () -> service.findById(id.get())
                        .orElseThrow(() -> new com.caronline.common.BizException(404, "乘客不存在"))
                        .toJson());
            } else {
                Handlers.run(exchange, () -> Json.array(service.list().stream().map(Passenger::toJson).toList()));
            }
            return;
        }
        if (Http.isMethod(exchange, "POST") && id.isEmpty()) {
            String body = Http.readBody(exchange);
            Handlers.run(exchange, () -> service.register(
                    Json.readString(body, "name"),
                    Json.readString(body, "phone")
            ).toJson());
            return;
        }
        Http.json(exchange, 405, Json.fail("只支持 GET / POST"));
    }
}
