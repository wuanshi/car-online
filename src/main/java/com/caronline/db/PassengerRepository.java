package com.caronline.db;

import com.caronline.model.Passenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
                list.add(new Passenger(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone")
                ));
            }
            return list;
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
}
