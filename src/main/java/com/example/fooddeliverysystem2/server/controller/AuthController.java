package com.example.fooddeliverysystem2.server.controller;

import com.example.fooddeliverysystem2.server.entity.User;
import com.example.fooddeliverysystem2.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        try {
            User user = userService.authenticate(username, password);
            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("fullName", user.getFullName());
                response.put("phone", user.getPhone());
                response.put("role", Map.of("id", user.getRole().getId(), "name", user.getRole().getName()));
                System.out.println("User logged in: " + username);
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Login failed for user: " + username);
                return ResponseEntity.status(401).body(Map.of("error", "Неверное имя пользователя или пароль"));
            }
        } catch (Exception e) {
            System.out.println("Login error for user " + username + ": " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка сервера: " + e.getMessage()));
        }
    }
}