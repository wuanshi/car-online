package com.caronline.model;

import com.caronline.http.Json;

public class User {
    private final int id;
    private final String name;
    private final String email;
    private final String password;
    private final String phone;
    private final String createdAt;
    private final String updatedAt;

    public User(int id, String name, String email, String password, String phone, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', password='" + password + "', phone='" + phone + "', createdAt='" + createdAt + "', updatedAt='" + updatedAt + "'}";
    }

    public String toJson() {
        return "{\"id\":" + id
                + ",\"name\":" + Json.quote(name)
                + ",\"email\":" + Json.quote(email)
                + ",\"password\":" + Json.quote(password)
                + ",\"phone\":" + Json.quote(phone)
                + "}";
    }
}