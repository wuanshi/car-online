package com.caronline.db;

import com.caronline.model.Passenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 乘客表的增 / 查。SQL 写在这里，Handler 只负责 HTTP。
 */
public class PassengerRepository {

    public List<Passenger> findAll() throws SQLException {
        String sql = "SELECT id, name, phone FROM passenger ORDER BY id";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Passenger> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    /**
     * 按主键查一条。查不到返回 {@link Optional#empty()}，避免返回 null。
     */
    public Optional<Passenger> findById(int id) throws SQLException {
        String sql = "SELECT id, name, phone FROM passenger WHERE id = ?";
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
        String sql = "SELECT id FROM passenger WHERE phone = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Passenger insert(String name, String phone) throws SQLException {
        String sql = "INSERT INTO passenger(name, phone) VALUES (?, ?)";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("插入成功但未返回自增 id");
                }
                return new Passenger(keys.getInt(1), name, phone);
            }
        }
    }

    private static Passenger mapRow(ResultSet rs) throws SQLException {
        return new Passenger(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone")
        );
    }
}
