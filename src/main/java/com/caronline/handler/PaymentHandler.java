package com.caronline.handler;

import com.caronline.http.Http;
import com.caronline.http.Json;
import com.caronline.model.Payment;
import com.caronline.service.OrderService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class PaymentHandler implements HttpHandler {

    private final OrderService service = new OrderService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Http.isPreflight(exchange)) {
            return;
        }
        if (Http.isMethod(exchange, "GET")) {
            Handlers.run(exchange, () -> Json.array(service.payments().stream().map(Payment::toJson).toList()));
            return;
        }
        Http.json(exchange, 405, Json.fail("只支持 GET"));
    }
}
