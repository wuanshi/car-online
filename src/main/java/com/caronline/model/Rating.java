package com.caronline.model;

import com.caronline.http.Json;

public class Rating {

    private final int id;
    private final int orderId;
    private final int stars;
    private final String comment;

    public Rating(int id, int orderId, int stars, String comment) {
        this.id = id;
        this.orderId = orderId;
        this.stars = stars;
        this.comment = comment;
    }

    public String toJson() {
        return "{\"id\":" + id
                + ",\"orderId\":" + orderId
                + ",\"stars\":" + stars
                + ",\"comment\":" + Json.quote(comment)
                + "}";
    }
}
