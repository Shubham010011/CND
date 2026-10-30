package com.cryptox.controller;

import com.cryptox.dto.*;
import com.cryptox.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    final AuthService s;

    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r) {
        try {
            return ResponseEntity.ok(s.register(r));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginRequest r) {
        try {
            return ResponseEntity.ok(s.login(r));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(java.util.Map.of("message", e.getMessage()));
        }
    }
}