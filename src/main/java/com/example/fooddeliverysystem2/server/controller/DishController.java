package com.example.fooddeliverysystem2.server.controller;

import com.example.fooddeliverysystem2.server.entity.Dish;
import com.example.fooddeliverysystem2.server.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    @GetMapping
    public ResponseEntity<List<Dish>> getAllDishes() {
        try {
            List<Dish> dishes = dishRepository.findAll();
            return ResponseEntity.ok(dishes);
        } catch (Exception e) {
            System.out.println("Error fetching dishes: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<Dish>> getAvailableDishes() {
        try {
            List<Dish> dishes = dishRepository.findByAvailableTrue();
            return ResponseEntity.ok(dishes);
        } catch (Exception e) {
            System.out.println("Error fetching available dishes: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDish(@RequestBody Map<String, Object> dishData) {
        try {
            Dish dish = new Dish();
            dish.setName((String) dishData.get("name"));
            dish.setDescription((String) dishData.get("description"));
            dish.setPrice(Double.parseDouble(dishData.get("price").toString()));
            dish.setAvailable(Boolean.parseBoolean(dishData.get("available").toString()));
            dishRepository.save(dish);
            return ResponseEntity.ok(Map.of("message", "Блюдо создано", "dishId", dish.getId()));
        } catch (Exception e) {
            System.out.println("Error creating dish: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка создания блюда: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDish(@PathVariable Long id, @RequestBody Map<String, Object> dishData) {
        try {
            Dish dish = dishRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Блюдо не найдено"));
            dish.setName((String) dishData.get("name"));
            dish.setDescription((String) dishData.get("description"));
            dish.setPrice(Double.parseDouble(dishData.get("price").toString()));
            dish.setAvailable(Boolean.parseBoolean(dishData.get("available").toString()));
            dishRepository.save(dish);
            return ResponseEntity.ok(Map.of("message", "Блюдо обновлено"));
        } catch (Exception e) {
            System.out.println("Error updating dish: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка обновления блюда: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDish(@PathVariable Long id) {
        try {
            Dish dish = dishRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Блюдо не найдено"));
            dishRepository.delete(dish);
            return ResponseEntity.ok(Map.of("message", "Блюдо удалено"));
        } catch (Exception e) {
            System.out.println("Error deleting dish: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка удаления блюда: " + e.getMessage()));
        }
    }
}