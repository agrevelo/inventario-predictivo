package com.inventario.backend.service;

import com.inventario.backend.dto.ProductoDTO;
import com.inventario.backend.dto.ProductoRequest;
import com.inventario.backend.model.Categoria;
import com.inventario.backend.model.Producto;
import com.inventario.backend.repository.CategoriaRepository;
import com.inventario.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor  // Lombok inyecta los repos por constructor (mejor práctica)
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;

    // ── LISTAR ────────────────────────────────────────────
    public List<ProductoDTO> listarTodos() {
        return productoRepo.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductoDTO> buscar(String nombre) {
        return productoRepo.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductoDTO buscarPorId(String id) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        return toDTO(p);
    }

    // ── CREAR ─────────────────────────────────────────────
    @Transactional  // si algo falla, revierte todo
    public ProductoDTO crear(ProductoRequest req) {
        Producto producto = Producto.builder()
                .nombre(req.getNombre())
                .descripcion(req.getDescripcion())
                .sku(req.getSku())
                .precio(req.getPrecio())
                .stockActual(req.getStockActual())
                .stockMinimo(req.getStockMinimo() != null ? req.getStockMinimo() : 5)
                .puntoReorden(req.getStockMinimo() != null ? req.getStockMinimo() * 2 : 10)
                .activo(true)
                .build();

        // Asigna categoría si se envió
        if (req.getCategoriaId() != null) {
            Categoria cat = categoriaRepo.findById(req.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            producto.setCategoria(cat);
        }

        return toDTO(productoRepo.save(producto));
    }

    // ── ACTUALIZAR ────────────────────────────────────────
    @Transactional
    public ProductoDTO actualizar(String id, ProductoRequest req) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        p.setNombre(req.getNombre());
        p.setDescripcion(req.getDescripcion());
        p.setSku(req.getSku());
        p.setPrecio(req.getPrecio());
        p.setStockActual(req.getStockActual());
        if (req.getStockMinimo() != null) p.setStockMinimo(req.getStockMinimo());

        return toDTO(productoRepo.save(p));
    }

    // ── ELIMINAR (soft delete) ────────────────────────────
    @Transactional
    public void eliminar(String id) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        p.setActivo(false);  // nunca borramos físicamente — mantenemos historial
        productoRepo.save(p);
    }

    // ── ALERTAS ───────────────────────────────────────────
    public List<ProductoDTO> getAlertas() {
        return productoRepo.findProductosStockCritico()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── MAPPER: Entidad → DTO ─────────────────────────────
    public ProductoDTO toDTO(Producto p) {
        return ProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .sku(p.getSku())
                .precio(p.getPrecio())
                .stockActual(p.getStockActual())
                .stockMinimo(p.getStockMinimo())
                .puntoReorden(p.getPuntoReorden())
                .stockSugeridoIa(p.getStockSugeridoIa())
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .categoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .activo(p.getActivo())
                .createdAt(p.getCreatedAt())
                .estadoStock(calcularEstadoStock(p))  // lógica de negocio aquí
                .build();
    }

    // Calcula si el stock está en estado crítico, bajo, normal o alto
    private String calcularEstadoStock(Producto p) {
        if (p.getStockActual() <= p.getStockMinimo())   return "CRITICO";
        if (p.getStockActual() <= p.getPuntoReorden())  return "BAJO";
        if (p.getStockActual() <= p.getPuntoReorden() * 2) return "NORMAL";
        return "ALTO";
    }
}