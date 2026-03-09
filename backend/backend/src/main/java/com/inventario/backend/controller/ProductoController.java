package com.inventario.backend.controller;

import com.inventario.backend.dto.ProductoDTO;
import com.inventario.backend.dto.ProductoRequest;
import com.inventario.backend.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // permite llamadas desde Next.js en dev
public class ProductoController {

    private final ProductoService productoService;

    // GET /api/v1/productos
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listar(
            @RequestParam(required = false) String buscar) {
        if (buscar != null && !buscar.isBlank()) {
            return ResponseEntity.ok(productoService.buscar(buscar));
        }
        return ResponseEntity.ok(productoService.listarTodos());
    }

    // GET /api/v1/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    // POST /api/v1/productos
    @PostMapping
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoRequest req) {
        return ResponseEntity.status(201).body(productoService.crear(req));
    }

    // PUT /api/v1/productos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ProductoRequest req) {
        return ResponseEntity.ok(productoService.actualizar(id, req));
    }

    // DELETE /api/v1/productos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    // GET /api/v1/productos/alertas
    @GetMapping("/alertas")
    public ResponseEntity<List<ProductoDTO>> alertas() {
        return ResponseEntity.ok(productoService.getAlertas());
    }
}