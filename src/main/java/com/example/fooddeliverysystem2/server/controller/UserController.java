package com.example.fooddeliverysystem2.server.controller;

import com.example.fooddeliverysystem2.server.entity.Role;
import com.example.fooddeliverysystem2.server.entity.User;
import com.example.fooddeliverysystem2.server.repository.RoleRepository;
import com.example.fooddeliverysystem2.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> response = users.stream().map(user -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("fullName", user.getFullName());
                userData.put("phone", user.getPhone());
                userData.put("roleName", user.getRole().getName());
                return userData;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error fetching users: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/couriers")
    public ResponseEntity<List<Map<String, Object>>> getCouriers() {
        try {
            List<User> couriers = userRepository.findAll().stream()
                    .filter(user -> "COURIER".equals(user.getRole().getName()))
                    .collect(Collectors.toList());
            List<Map<String, Object>> response = couriers.stream().map(user -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("fullName", user.getFullName());
                return userData;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error fetching couriers: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        try {
            String username = (String) userData.get("username");
            String password = (String) userData.get("password");
            String fullName = (String) userData.get("fullName");
            Long roleId = Long.valueOf(userData.get("roleId").toString());

            if (userRepository.findByUsername(username).isPresent()) {
                return ResponseEntity.status(400).body(Map.of("error", "Пользователь с таким именем уже существует"));
            }

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));

            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setRole(role);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Пользователь создан", "userId", user.getId()));
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка создания пользователя: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            String username = (String) userData.get("username");
            String fullName = (String) userData.get("fullName");
            String phone = (String) userData.get("phone");
            Long roleId = Long.valueOf(userData.get("roleId").toString());
            String password = userData.containsKey("password") ? (String) userData.get("password") : null;

            if (!user.getUsername().equals(username) && userRepository.findByUsername(username).isPresent()) {
                return ResponseEntity.status(400).body(Map.of("error", "Пользователь с таким именем уже существует"));
            }

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));

            user.setUsername(username);
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole(role);
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Пользователь обновлен"));
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка обновления пользователя: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            userRepository.delete(user);
            return ResponseEntity.ok(Map.of("message", "Пользователь удалён"));
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка удаления пользователя: " + e.getMessage()));
        }
    }
}