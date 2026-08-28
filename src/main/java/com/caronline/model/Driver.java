package com.caronline.model;

import com.caronline.http.Json;

/**
 * 司机。通过 {@link #vehicle} 引用自己的车，这就是对象之间的组合。
 */
public class Driver {

    private final int id;
    private final String name;
    private final String phone;
    private final Vehicle vehicle;

    public Driver(int id, String name, String phone, Vehicle vehicle) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.vehicle = vehicle;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String toJson() {
        return "{\"id\":" + id
                + ",\"name\":" + Json.quote(name)
                + ",\"phone\":" + Json.quote(phone)
                + ",\"vehicle\":" + vehicle.toJson()
                + "}";
    }
}
