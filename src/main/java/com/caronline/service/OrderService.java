package com.caronline.service;

import com.caronline.common.BizException;
import com.caronline.db.OrderRepository;
import com.caronline.db.PaymentRepository;
import com.caronline.db.RatingRepository;
import com.caronline.model.OrderStatus;
import com.caronline.model.Payment;
import com.caronline.model.Rating;
import com.caronline.model.RideOrder;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class OrderService {

    private final OrderRepository orders = new OrderRepository();
    private final PaymentRepository payments = new PaymentRepository();
    private final RatingRepository ratings = new RatingRepository();
    private final PassengerService passengers = new PassengerService();
    private final DriverService drivers = new DriverService();

    public List<RideOrder> list(OrderStatus status) throws SQLException {
        return orders.findAll(status);
    }

    public RideOrder get(int id) throws SQLException {
        return orders.findById(id).orElseThrow(() -> new BizException(404, "订单不存在"));
    }

    public RideOrder create(Integer passengerId, String origin, String destination, BigDecimal distanceKm) throws SQLException {
        if (passengerId == null || passengerId <= 0) {
            throw new BizException("passengerId 不能为空");
        }
        if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            throw new BizException("origin 和 destination 不能为空");
        }
        if (distanceKm == null || distanceKm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("distanceKm 必须大于 0");
        }
        passengers.requireExists(passengerId);
        if (orders.hasInProgressByPassenger(passengerId)) {
            throw new BizException("该乘客已有进行中的订单");
        }
        return orders.insert(passengerId, origin.trim(), destination.trim(), distanceKm);
    }

    public RideOrder cancel(int id) throws SQLException {
        RideOrder order = get(id);
        if (order.getStatus() != OrderStatus.WAITING_ACCEPT) {
            throw new BizException("只有待接单可以取消");
        }
        orders.updateStatus(id, OrderStatus.CANCELLED);
        return get(id);
    }

    public RideOrder accept(int id, Integer driverId) throws SQLException {
        if (driverId == null || driverId <= 0) {
            throw new BizException("driverId 不能为空");
        }
        RideOrder order = get(id);
        if (order.getStatus() != OrderStatus.WAITING_ACCEPT) {
            throw new BizException("当前订单不是待接单状态");
        }
        drivers.requireExists(driverId);
        if (orders.hasInProgressByDriver(driverId)) {
            throw new BizException("该司机正在服务其他订单");
        }
        if (!orders.accept(id, driverId)) {
            throw new BizException("接单失败，可能已被其他司机接走");
        }
        return get(id);
    }

    public RideOrder arrive(int id) throws SQLException {
        return transit(id, OrderStatus.ACCEPTED, OrderStatus.DRIVER_ARRIVED, "只有已接单才能确认到达上车点");
    }

    public RideOrder start(int id) throws SQLException {
        return transit(id, OrderStatus.DRIVER_ARRIVED, OrderStatus.IN_TRIP, "只有到达上车点后才能开始行程");
    }

    public RideOrder finish(int id) throws SQLException {
        RideOrder order = get(id);
        if (order.getStatus() != OrderStatus.IN_TRIP) {
            throw new BizException("只有行程中才能结束订单");
        }
        BigDecimal fare = FareCalculator.calc(order.getDistanceKm());
        orders.complete(id, fare);
        return get(id);
    }

    public String fareDetail(int id) throws SQLException {
        RideOrder order = get(id);
        BigDecimal fare = order.getFare() != null ? order.getFare() : FareCalculator.calc(order.getDistanceKm());
        return FareCalculator.detailJson(order.getDistanceKm(), fare);
    }

    public Payment pay(int id) throws SQLException {
        RideOrder order = get(id);
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BizException("订单未完成，不能支付");
        }
        if (order.isPaid() || payments.existsByOrderId(id)) {
            throw new BizException("该订单已支付");
        }
        BigDecimal fare = order.getFare() != null ? order.getFare() : FareCalculator.calc(order.getDistanceKm());
        Payment payment = payments.insert(id, fare);
        orders.markPaid(id);
        return payment;
    }

    public List<Payment> payments() throws SQLException {
        return payments.findAll();
    }

    public Rating rate(int id, Integer stars, String comment) throws SQLException {
        RideOrder order = get(id);
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BizException("未完成订单不能评价");
        }
        if (!order.isPaid()) {
            throw new BizException("请先支付再评价");
        }
        if (ratings.existsByOrderId(id)) {
            throw new BizException("该订单已评价");
        }
        if (stars == null || stars < 1 || stars > 5) {
            throw new BizException("stars 必须是 1～5");
        }
        String text = comment == null ? "" : comment.trim();
        return ratings.insert(id, stars, text);
    }

    public Rating ratingOf(int id) throws SQLException {
        get(id);
        return ratings.findByOrderId(id).orElseThrow(() -> new BizException(404, "该订单还没有评价"));
    }

    private RideOrder transit(int id, OrderStatus expected, OrderStatus next, String message) throws SQLException {
        RideOrder order = get(id);
        if (order.getStatus() != expected) {
            throw new BizException(message);
        }
        orders.updateStatus(id, next);
        return get(id);
    }
}
