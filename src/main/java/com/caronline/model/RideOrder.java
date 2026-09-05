package com.caronline.model;

import com.caronline.http.Json;

import java.math.BigDecimal;

/**
 * 一次打车订单。
 */
public class RideOrder {

    private final int id;
    private final int passengerId;
    private final Integer driverId;
    private final String origin;
    private final String destination;
    private final BigDecimal distanceKm;
    private final OrderStatus status;
    private final BigDecimal fare;
    private final boolean paid;
    private final String passengerName;
    private final String driverName;
    private final String plate;

    public RideOrder(
            int id,
            int passengerId,
            Integer driverId,
            String origin,
            String destination,
            BigDecimal distanceKm,
            OrderStatus status,
            BigDecimal fare,
            boolean paid,
            String passengerName,
            String driverName,
            String plate
    ) {
        this.id = id;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.status = status;
        this.fare = fare;
        this.paid = paid;
        this.passengerName = passengerName;
        this.driverName = driverName;
        this.plate = plate;
    }

    public int getId() {
        return id;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getFare() {
        return fare;
    }

    public boolean isPaid() {
        return paid;
    }

    public String toJson() {
        return "{\"id\":" + id
                + ",\"passengerId\":" + passengerId
                + ",\"driverId\":" + (driverId == null ? "null" : driverId)
                + ",\"origin\":" + Json.quote(origin)
                + ",\"destination\":" + Json.quote(destination)
                + ",\"distanceKm\":" + distanceKm.toPlainString()
                + ",\"status\":" + Json.quote(status.name())
                + ",\"fare\":" + (fare == null ? "null" : fare.toPlainString())
                + ",\"paid\":" + paid
                + ",\"passengerName\":" + Json.quote(passengerName)
                + ",\"driverName\":" + Json.quote(driverName)
                + ",\"plate\":" + Json.quote(plate)
                + "}";
    }
}
