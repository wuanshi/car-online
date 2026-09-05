package com.caronline.model;

/**
 * 订单状态。只用枚举，避免满地写字符串。
 */
public enum OrderStatus {
    WAITING_ACCEPT,
    ACCEPTED,
    DRIVER_ARRIVED,
    IN_TRIP,
    COMPLETED,
    CANCELLED;

    public boolean inProgress() {
        return this == WAITING_ACCEPT
                || this == ACCEPTED
                || this == DRIVER_ARRIVED
                || this == IN_TRIP;
    }
}
