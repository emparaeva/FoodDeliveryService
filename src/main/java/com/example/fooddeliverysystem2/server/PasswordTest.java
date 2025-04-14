package com.example.fooddeliverysystem2.server;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = "$2a$10$VV99kYczbNNX9cTW1VNhq.e2sWT4VXx7NijBOjXhXzqfC5sLi2i12";
        System.out.println("Password matches: " + encoder.matches(password, hash));
    }
}
