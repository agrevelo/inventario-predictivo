package com.inventario.backend.controller;

import com.inventario.backend.dto.*;
import com.inventario.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // POST /api/v1/auth/register — crea una cuenta nueva y retorna token
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(
            @Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(201).body(authService.registrar(req));
    }

    // POST /api/v1/auth/login — autentica y retorna token
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}