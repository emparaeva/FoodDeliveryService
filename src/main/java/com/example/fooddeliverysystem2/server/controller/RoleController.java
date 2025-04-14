package com.example.fooddeliverysystem2.server.controller;

import com.example.fooddeliverysystem2.server.entity.Role;
import com.example.fooddeliverysystem2.server.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllRoles() {
        try {
            List<Role> roles = roleRepository.findAll();
            List<Map<String, Object>> response = roles.stream().map(role -> {
                Map<String, Object> roleData = new HashMap<>();
                roleData.put("id", role.getId());
                roleData.put("name", role.getName());
                return roleData;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error fetching roles: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}