package com.caronline.model;

import com.caronline.http.Json;

/**
 * 车辆。一个司机绑定一辆车：Driver 里持有 Vehicle 引用。
 */
public class Vehicle {

    private final int id;
    private final String plate;
    private final String brand;
    private final String color;

    public Vehicle(int id, String plate, String brand, String color) {
        this.id = id;
        this.plate = plate;
        this.brand = brand;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public String getPlate() {
        return plate;
    }

    public String getBrand() {
        return brand;
    }

    public String getColor() {
        return color;
    }

    public String toJson() {
        return "{\"id\":" + id
                + ",\"plate\":" + Json.quote(plate)
                + ",\"brand\":" + Json.quote(brand)
                + ",\"color\":" + Json.quote(color)
                + "}";
    }
}
