package com.example.fooddeliverysystem2.server.repository;

import com.example.fooddeliverysystem2.server.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByAvailableTrue();
}