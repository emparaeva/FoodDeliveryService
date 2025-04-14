package com.example.fooddeliverysystem2.server.controller;

import com.example.fooddeliverysystem2.server.entity.Order;
import com.example.fooddeliverysystem2.server.entity.OrderDish;
import com.example.fooddeliverysystem2.server.entity.User;
import com.example.fooddeliverysystem2.server.repository.DishRepository;
import com.example.fooddeliverysystem2.server.repository.OrderRepository;
import com.example.fooddeliverysystem2.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData) {
        try {
            Long userId = Long.valueOf(orderData.get("userId").toString());
            String deliveryAddress = (String) orderData.get("deliveryAddress");
            String orderDateStr = (String) orderData.get("orderDate");
            List<Map<String, Object>> dishes = (List<Map<String, Object>>) orderData.get("dishes");
            System.out.println("Received deliveryAddress: " + deliveryAddress);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            Order order = new Order();
            order.setUser(user);
            order.setDeliveryAddress(deliveryAddress);
            order.setStatus("NEW");
            order.setOrderDate(orderDateStr != null ? LocalDateTime.parse(orderDateStr) : LocalDateTime.now());
            List<OrderDish> orderDishes = new ArrayList<>();
            double totalPrice = 0.0;

            for (Map<String, Object> dishData : dishes) {
                Long dishId = Long.valueOf(dishData.get("dishId").toString());
                Integer quantity = Integer.valueOf(dishData.get("quantity").toString());
                com.example.fooddeliverysystem2.server.entity.Dish dish = dishRepository.findById(dishId)
                        .orElseThrow(() -> new IllegalArgumentException("Блюдо не найдено"));
                if (!dish.getAvailable()) {
                    throw new IllegalArgumentException("Блюдо " + dish.getName() + " недоступно");
                }
                OrderDish orderDish = new OrderDish();
                orderDish.setOrder(order);
                orderDish.setDish(dish);
                orderDish.setQuantity(quantity);
                orderDishes.add(orderDish);
                totalPrice += dish.getPrice() * quantity;
            }

            order.setDishes(orderDishes);
            order.setTotalPrice(totalPrice);
            orderRepository.save(order);
            System.out.println("Order created successfully: " + order.getId() + " with address: " + deliveryAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Заказ создан");
            response.put("orderId", order.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка создания заказа: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserOrders(@PathVariable Long userId) {
        try {
            System.out.println("Fetching orders for userId: " + userId);
            List<Order> orders = orderRepository.findByUserId(userId);
            List<Map<String, Object>> response = orders.stream().map(order -> {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.getId());
                orderData.put("status", order.getStatus());
                orderData.put("totalPrice", order.getTotalPrice());
                orderData.put("deliveryAddress", order.getDeliveryAddress());
                orderData.put("courierId", order.getCourier() != null ? order.getCourier().getId() : null);
                orderData.put("courierName", order.getCourier() != null ? order.getCourier().getFullName() : null);
                orderData.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
                orderData.put("deliveredAt", order.getDeliveredAt() != null ? order.getDeliveredAt().toString() : null); // Добавлено
                List<Map<String, Object>> dishes = order.getDishes().stream().map(orderDish -> {
                    Map<String, Object> dishData = new HashMap<>();
                    dishData.put("name", orderDish.getDish().getName());
                    dishData.put("quantity", orderDish.getQuantity());
                    return dishData;
                }).collect(Collectors.toList());
                orderData.put("dishes", dishes);
                return orderData;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error fetching orders for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of(Map.of("error", "Ошибка получения заказов: " + e.getMessage())));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        try {
            System.out.println("Fetching all orders");
            List<Order> orders = orderRepository.findAll();
            List<Map<String, Object>> response = orders.stream().map(order -> {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.getId());
                orderData.put("status", order.getStatus());
                orderData.put("totalPrice", order.getTotalPrice());
                orderData.put("deliveryAddress", order.getDeliveryAddress());
                orderData.put("courierId", order.getCourier() != null ? order.getCourier().getId() : null);
                orderData.put("courierName", order.getCourier() != null ? order.getCourier().getFullName() : null);
                orderData.put("createdAt", order.getCreatedAt().toString());
                return orderData;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error fetching all orders: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of(Map.of("error", "Ошибка получения заказов: " + e.getMessage())));
        }
    }

    @GetMapping("/courier/{courierId}")
    public ResponseEntity<List<Map<String, Object>>> getCourierOrders(@PathVariable Long courierId) {
        try {
            System.out.println("Fetching orders for courierId: " + courierId);
            List<Order> orders = orderRepository.findByCourierId(courierId);
            List<Map<String, Object>> response = orders.stream().map(order -> {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.getId());
                orderData.put("status", order.getStatus());
                orderData.put("totalPrice", order.getTotalPrice());
                orderData.put("deliveryAddress", order.getDeliveryAddress());
                orderData.put("createdAt", order.getCreatedAt().toString());
                return orderData;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error fetching orders for courierId " + courierId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of(Map.of("error", "Ошибка получения заказов: " + e.getMessage())));
        }
    }

    @PostMapping("/{id}/setCanceled")
    public ResponseEntity<Map<String, Object>> setOrderCanceled(@PathVariable Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
            if (!"NEW".equals(order.getStatus()) && !"ASSIGNED".equals(order.getStatus())) {
                return ResponseEntity.status(400).body(Map.of("error", "Можно отменить только новый или назначенный заказ"));
            }
            order.setStatus("CANCELED");
            orderRepository.save(order);
            System.out.println("Order canceled: " + id);
            return ResponseEntity.ok(Map.of("message", "Заказ отменён"));
        } catch (Exception e) {
            System.out.println("Error canceling order " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка отмены заказа: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
            if (!"CANCELED".equals(order.getStatus())) {
                return ResponseEntity.status(400).body(Map.of("error", "Можно удалить только отменённый заказ"));
            }
            orderRepository.delete(order);
            System.out.println("Order deleted: " + id);
            return ResponseEntity.ok(Map.of("message", "Заказ удалён"));
        } catch (Exception e) {
            System.out.println("Error deleting order " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка удаления заказа: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<Map<String, Object>> assignCourier(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
            if (!"NEW".equals(order.getStatus())) {
                return ResponseEntity.status(400).body(Map.of("error", "Можно назначить курьера только на новый заказ"));
            }
            Long courierId = Long.valueOf(data.get("courierId").toString());
            User courier = userRepository.findById(courierId)
                    .orElseThrow(() -> new IllegalArgumentException("Курьер не найден"));
            if (!"COURIER".equals(courier.getRole().getName())) {
                return ResponseEntity.status(400).body(Map.of("error", "Пользователь не является курьером"));
            }
            order.setCourier(courier);
            order.setStatus("ASSIGNED");
            orderRepository.save(order);
            System.out.println("Courier assigned to order " + id + ": " + courierId);
            return ResponseEntity.ok(Map.of("message", "Курьер назначен"));
        } catch (Exception e) {
            System.out.println("Error assigning courier to order " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка назначения курьера: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/delivered")
    public ResponseEntity<Map<String, Object>> markDelivered(@PathVariable Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
            if (!"ASSIGNED".equals(order.getStatus())) {
                return ResponseEntity.status(400).body(Map.of("error", "Можно отметить доставленным только назначенный заказ"));
            }
            order.setStatus("DELIVERED");
            order.setDeliveredAt(LocalDateTime.now());
            orderRepository.save(order);
            System.out.println("Order marked delivered: " + id);
            return ResponseEntity.ok(Map.of("message", "Заказ доставлен"));
        } catch (Exception e) {
            System.out.println("Error marking order delivered " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка отметки доставки: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        try {
            List<Order> orders = orderRepository.findAll();
            long totalOrders = orders.size();

            // Подсчет заказов по статусам
            Map<String, Long> statusCounts = orders.stream()
                    .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

            // Среднее время доставки (для заказов со статусом DELIVERED и непустым delivered_at)
            double avgDeliveryTime = orders.stream()
                    .filter(order -> "DELIVERED".equals(order.getStatus()) && order.getDeliveredAt() != null)
                    .mapToLong(order -> Duration.between(order.getCreatedAt(), order.getDeliveredAt()).toMinutes())
                    .average()
                    .orElse(0.0);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", totalOrders);
            stats.put("statusCounts", statusCounts);
            stats.put("avgDeliveryTime", avgDeliveryTime);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.out.println("Error fetching order stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка получения статистики: " + e.getMessage()));
        }
    }
}


