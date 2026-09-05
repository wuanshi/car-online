package com.caronline.db;

import com.caronline.model.Rating;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class RatingRepository {

    public boolean existsByOrderId(int orderId) throws SQLException {
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM rating WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Rating insert(int orderId, int stars, String comment) throws SQLException {
        String sql = "INSERT INTO rating(order_id, stars, comment) VALUES (?, ?, ?)";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orderId);
            ps.setInt(2, stars);
            ps.setString(3, comment);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("插入评价失败");
                }
                return new Rating(keys.getInt(1), orderId, stars, comment);
            }
        }
    }

    public Optional<Rating> findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT id, order_id, stars, comment FROM rating WHERE order_id = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Rating(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("stars"),
                        rs.getString("comment")
                ));
            }
        }
    }
}
