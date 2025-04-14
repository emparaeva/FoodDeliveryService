package com.example.fooddeliverysystem2.server.repository;

import com.example.fooddeliverysystem2.server.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);
    List<Order> findByCourierId(Long courierId);
}