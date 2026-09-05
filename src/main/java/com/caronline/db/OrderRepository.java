package com.caronline.db;

import com.caronline.model.OrderStatus;
import com.caronline.model.RideOrder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository {

    private static final String SELECT = """
            SELECT o.id, o.passenger_id, o.driver_id, o.origin, o.destination,
                   o.distance_km, o.status, o.fare, o.paid,
                   p.name AS passenger_name,
                   d.name AS driver_name,
                   v.plate
            FROM ride_order o
            JOIN passenger p ON p.id = o.passenger_id
            LEFT JOIN driver d ON d.id = o.driver_id
            LEFT JOIN vehicle v ON v.driver_id = o.driver_id
            """;

    public List<RideOrder> findAll(OrderStatus status) throws SQLException {
        String sql = SELECT + (status == null ? " ORDER BY o.id DESC" : " WHERE o.status = ? ORDER BY o.id DESC");
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (status != null) {
                ps.setString(1, status.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<RideOrder> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        }
    }

    public Optional<RideOrder> findById(int id) throws SQLException {
        String sql = SELECT + " WHERE o.id = ?";
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

    public boolean hasInProgressByPassenger(int passengerId) throws SQLException {
        String sql = """
                SELECT id FROM ride_order
                WHERE passenger_id = ?
                  AND status IN ('WAITING_ACCEPT','ACCEPTED','DRIVER_ARRIVED','IN_TRIP')
                """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasInProgressByDriver(int driverId) throws SQLException {
        String sql = """
                SELECT id FROM ride_order
                WHERE driver_id = ?
                  AND status IN ('ACCEPTED','DRIVER_ARRIVED','IN_TRIP')
                """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public RideOrder insert(int passengerId, String origin, String destination, BigDecimal distanceKm) throws SQLException {
        String sql = """
                INSERT INTO ride_order(passenger_id, origin, destination, distance_km, status, paid)
                VALUES (?, ?, ?, ?, ?, 0)
                """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, passengerId);
            ps.setString(2, origin);
            ps.setString(3, destination);
            ps.setBigDecimal(4, distanceKm);
            ps.setString(5, OrderStatus.WAITING_ACCEPT.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("插入订单成功但未返回 id");
                }
                return findById(keys.getInt(1)).orElseThrow();
            }
        }
    }

    public void updateStatus(int id, OrderStatus status) throws SQLException {
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE ride_order SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean accept(int orderId, int driverId) throws SQLException {
        String sql = """
                UPDATE ride_order
                SET driver_id = ?, status = ?
                WHERE id = ? AND status = ?
                """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ps.setString(2, OrderStatus.ACCEPTED.name());
            ps.setInt(3, orderId);
            ps.setString(4, OrderStatus.WAITING_ACCEPT.name());
            return ps.executeUpdate() == 1;
        }
    }

    public void complete(int id, BigDecimal fare) throws SQLException {
        String sql = "UPDATE ride_order SET status = ?, fare = ? WHERE id = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, OrderStatus.COMPLETED.name());
            ps.setBigDecimal(2, fare);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void markPaid(int id) throws SQLException {
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE ride_order SET paid = 1 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static RideOrder mapRow(ResultSet rs) throws SQLException {
        int driverId = rs.getInt("driver_id");
        Integer driver = rs.wasNull() ? null : driverId;
        BigDecimal fare = rs.getBigDecimal("fare");
        return new RideOrder(
                rs.getInt("id"),
                rs.getInt("passenger_id"),
                driver,
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getBigDecimal("distance_km"),
                OrderStatus.valueOf(rs.getString("status")),
                fare,
                rs.getBoolean("paid"),
                rs.getString("passenger_name"),
                rs.getString("driver_name"),
                rs.getString("plate")
        );
    }
}
