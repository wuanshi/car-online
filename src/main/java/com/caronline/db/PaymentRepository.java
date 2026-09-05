package com.caronline.db;

import com.caronline.model.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentRepository {

    public boolean existsByOrderId(int orderId) throws SQLException {
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM payment WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Payment insert(int orderId, BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO payment(order_id, amount) VALUES (?, ?)";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orderId);
            ps.setBigDecimal(2, amount);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("插入支付流水失败");
                }
                return new Payment(keys.getInt(1), orderId, amount);
            }
        }
    }

    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT id, order_id, amount FROM payment ORDER BY id DESC";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Payment> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Payment(rs.getInt("id"), rs.getInt("order_id"), rs.getBigDecimal("amount")));
            }
            return list;
        }
    }

    public Optional<Payment> findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT id, order_id, amount FROM payment WHERE order_id = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Payment(rs.getInt("id"), rs.getInt("order_id"), rs.getBigDecimal("amount")));
            }
        }
    }
}
