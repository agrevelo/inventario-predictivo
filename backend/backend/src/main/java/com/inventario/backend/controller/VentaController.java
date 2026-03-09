package com.inventario.backend.controller;

import com.inventario.backend.dto.VentaRequest;
import com.inventario.backend.model.Venta;
import com.inventario.backend.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VentaController {

    private final VentaService ventaService;

    // POST /api/v1/ventas
    @PostMapping
    public ResponseEntity<Venta> registrar(@Valid @RequestBody VentaRequest req) {
        return ResponseEntity.status(201).body(ventaService.registrarVenta(req));
    }
}