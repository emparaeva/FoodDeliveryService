package com.example.fooddeliverysystem2.server.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderDishId implements Serializable {

    private Long orderId;

    private Long dishId;

    public OrderDishId() {}

    public OrderDishId(Long orderId, Long dishId) {
        this.orderId = orderId;
        this.dishId = dishId;
    }

    // Геттеры и сеттеры
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDishId that = (OrderDishId) o;
        return Objects.equals(orderId, that.orderId) &&
                Objects.equals(dishId, that.dishId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, dishId);
    }
}
