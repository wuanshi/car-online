package com.caronline.handler;

import com.caronline.http.Http;
import com.caronline.http.Json;
import com.caronline.http.Paths;
import com.caronline.model.OrderStatus;
import com.caronline.model.RideOrder;
import com.caronline.service.OrderService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * 订单全流程：发单、取消、接单、到达、开始、结束、费用、支付、评价。
 */
public class OrderHandler implements HttpHandler {

    private final OrderService service = new OrderService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Http.isPreflight(exchange)) {
            return;
        }

        Paths.ParsedPath route;
        try {
            route = Paths.parse(exchange.getRequestURI().getPath(), "/api/orders");
        } catch (IllegalArgumentException e) {
            Http.json(exchange, 400, Json.fail(e.getMessage()));
            return;
        }

        String action = route.action().orElse("");
        boolean get = Http.isMethod(exchange, "GET");
        boolean post = Http.isMethod(exchange, "POST");

        if (get && route.id().isEmpty() && action.isEmpty()) {
            String query = exchange.getRequestURI().getQuery();
            Handlers.run(exchange, () -> {
                OrderStatus status = parseStatus(Paths.queryParam(query, "status"));
                return Json.array(service.list(status).stream().map(RideOrder::toJson).toList());
            });
            return;
        }
        if (get && route.id().isPresent() && action.isEmpty()) {
            Handlers.run(exchange, () -> service.get(route.id().get()).toJson());
            return;
        }
        if (get && route.id().isPresent() && "fare".equals(action)) {
            Handlers.run(exchange, () -> service.fareDetail(route.id().get()));
            return;
        }
        if (get && route.id().isPresent() && "rating".equals(action)) {
            Handlers.run(exchange, () -> service.ratingOf(route.id().get()).toJson());
            return;
        }
        if (post && route.id().isEmpty() && action.isEmpty()) {
            String body = Http.readBody(exchange);
            Handlers.run(exchange, () -> service.create(
                    Json.readInt(body, "passengerId"),
                    Json.readString(body, "origin"),
                    Json.readString(body, "destination"),
                    Json.readDecimal(body, "distanceKm")
            ).toJson());
            return;
        }
        if (post && route.id().isPresent()) {
            int id = route.id().get();
            String body = Http.readBody(exchange);
            switch (action) {
                case "cancel" -> Handlers.run(exchange, () -> service.cancel(id).toJson());
                case "accept" -> Handlers.run(exchange, () -> service.accept(id, Json.readInt(body, "driverId")).toJson());
                case "arrive" -> Handlers.run(exchange, () -> service.arrive(id).toJson());
                case "start" -> Handlers.run(exchange, () -> service.start(id).toJson());
                case "finish" -> Handlers.run(exchange, () -> service.finish(id).toJson());
                case "pay" -> Handlers.run(exchange, () -> service.pay(id).toJson());
                case "rating" -> Handlers.run(exchange, () -> service.rate(
                        id,
                        Json.readInt(body, "stars"),
                        Json.readString(body, "comment")
                ).toJson());
                default -> Http.json(exchange, 404, Json.fail("接口不存在"));
            }
            return;
        }
        Http.json(exchange, 405, Json.fail("不支持的请求"));
    }

    private static OrderStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new com.caronline.common.BizException("未知订单状态");
        }
    }
}
