package com.caronline.service;

import com.caronline.common.BizException;
import com.caronline.db.PassengerRepository;
import com.caronline.model.Passenger;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PassengerService {

    private final PassengerRepository repository = new PassengerRepository();

    public List<Passenger> list() throws SQLException {
        return repository.findAll();
    }

    public Optional<Passenger> findById(int id) throws SQLException {
        return repository.findById(id);
    }

    public Passenger register(String name, String phone) throws SQLException {
        if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
            throw new BizException("name 和 phone 不能为空，JSON 示例：{\"name\":\"张三\",\"phone\":\"13800000000\"}");
        }
        if (repository.existsByPhone(phone.trim())) {
            throw new BizException("手机号已存在");
        }
        return repository.insert(name.trim(), phone.trim());
    }

    public void requireExists(int id) throws SQLException {
        if (repository.findById(id).isEmpty()) {
            throw new BizException(404, "乘客不存在");
        }
    }
}
