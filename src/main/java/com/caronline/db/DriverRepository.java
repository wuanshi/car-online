package com.caronline.db;

import com.caronline.model.Driver;
import com.caronline.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 司机 + 车辆。注册时两张表一起写，用同一条 Connection 保证同时成功或同时失败。
 */
public class DriverRepository {

    public List<Driver> findAll() throws SQLException {
        String sql = """
                SELECT d.id, d.name, d.phone,
                       v.id AS vehicle_id, v.plate, v.brand, v.color
                FROM driver d
                INNER JOIN vehicle v ON v.driver_id = d.id
                ORDER BY d.id
                """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Driver> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    public Optional<Driver> findById(int id) throws SQLException {
        String sql = """
                SELECT d.id, d.name, d.phone,
                       v.id AS vehicle_id, v.plate, v.brand, v.color
                FROM driver d
                INNER JOIN vehicle v ON v.driver_id = d.id
                WHERE d.id = ?
                """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    public boolean existsByPhone(String phone) throws SQLException {
        String sql = "SELECT id FROM driver WHERE phone = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsByPlate(String plate) throws SQLException {
        String sql = "SELECT id FROM vehicle WHERE plate = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Driver insert(String name, String phone, String plate, String brand, String color) throws SQLException {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int driverId;
                String insertDriver = "INSERT INTO driver(name, phone) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertDriver, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, phone);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("插入司机成功但未返回 id");
                        }
                        driverId = keys.getInt(1);
                    }
                }

                int vehicleId;
                String insertVehicle = "INSERT INTO vehicle(driver_id, plate, brand, color) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertVehicle, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, driverId);
                    ps.setString(2, plate);
                    ps.setString(3, brand);
                    ps.setString(4, color);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("插入车辆成功但未返回 id");
                        }
                        vehicleId = keys.getInt(1);
                    }
                }

                conn.commit();
                Vehicle vehicle = new Vehicle(vehicleId, plate, brand, color);
                return new Driver(driverId, name, phone, vehicle);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static Driver mapRow(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle(
                rs.getInt("vehicle_id"),
                rs.getString("plate"),
                rs.getString("brand"),
                rs.getString("color")
        );
        return new Driver(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                vehicle
        );
    }
}
