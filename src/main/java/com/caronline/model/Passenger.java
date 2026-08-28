package com.caronline.model;

import com.caronline.http.Json;

/**
 * 乘客。字段对应 MySQL 表 passenger 的一列。
 */
public class Passenger {

    private final int id;
    private final String name;
    private final String phone;

    public Passenger(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
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

    public String toJson() {
        return "{\"id\":" + id
                + ",\"name\":" + Json.quote(name)
                + ",\"phone\":" + Json.quote(phone)
                + "}";
    }

    @Override
    public String toString() {
        return "Passenger{id=" + id + ", name='" + name + "', phone='" + phone + "'}";
    }
}
