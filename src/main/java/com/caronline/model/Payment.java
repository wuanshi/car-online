package com.caronline.model;

import com.caronline.http.Json;

import java.math.BigDecimal;

public class Payment {

    private final int id;
    private final int orderId;
    private final BigDecimal amount;

    public Payment(int id, int orderId, BigDecimal amount) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
    }

    public String toJson() {
        return "{\"id\":" + id
                + ",\"orderId\":" + orderId
                + ",\"amount\":" + amount.toPlainString()
                + "}";
    }
}
