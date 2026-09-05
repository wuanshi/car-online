package com.caronline.service;

import com.caronline.common.BizException;
import com.caronline.db.DriverRepository;
import com.caronline.model.Driver;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DriverService {

    private final DriverRepository repository = new DriverRepository();

    public List<Driver> list() throws SQLException {
        return repository.findAll();
    }

    public Optional<Driver> findById(int id) throws SQLException {
        return repository.findById(id);
    }

    public Driver register(String name, String phone, String plate, String brand, String color) throws SQLException {
        if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
            throw new BizException("name 和 phone 不能为空");
        }
        if (plate == null || plate.isBlank()) {
            throw new BizException("plate（车牌）不能为空，JSON 示例：{\"name\":\"李四\",\"phone\":\"13900000000\",\"plate\":\"粤A12345\",\"brand\":\"比亚迪\",\"color\":\"白\"}");
        }
        if (repository.existsByPhone(phone.trim())) {
            throw new BizException("手机号已存在");
        }
        if (repository.existsByPlate(plate.trim())) {
            throw new BizException("车牌已被占用");
        }
        String safeBrand = brand == null || brand.isBlank() ? "" : brand.trim();
        String safeColor = color == null || color.isBlank() ? "" : color.trim();
        return repository.insert(name.trim(), phone.trim(), plate.trim(), safeBrand, safeColor);
    }

    public void requireExists(int id) throws SQLException {
        if (repository.findById(id).isEmpty()) {
            throw new BizException(404, "司机不存在");
        }
    }
}
