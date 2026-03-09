package com.inventario.backend.service;

import com.inventario.backend.dto.VentaRequest;
import com.inventario.backend.model.*;
import com.inventario.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final VentaDetalleRepository detalleRepo;

    @Transactional
    public Venta registrarVenta(VentaRequest req) {

        // 1. Valida que el número de factura no exista
        if (req.getNumeroFactura() != null &&
                ventaRepo.existsByNumeroFactura(req.getNumeroFactura())) {
            throw new RuntimeException("Ya existe una venta con la factura: " + req.getNumeroFactura());
        }

        // 2. Construye la cabecera de la venta
        Venta venta = Venta.builder()
                .fechaVenta(req.getFechaVenta())
                .numeroFactura(req.getNumeroFactura())
                .build();

        BigDecimal totalVenta = BigDecimal.ZERO;
        List<VentaDetalle> detalles = new ArrayList<>();

        // 3. Procesa cada línea de la venta
        for (VentaRequest.VentaDetalleRequest item : req.getDetalles()) {

            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado: " + item.getProductoId()));

            // Valida stock suficiente
            if (producto.getStockActual() < item.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para: " + producto.getNombre() +
                                ". Disponible: " + producto.getStockActual());
            }

            // Descuenta el stock — operación crítica
            producto.setStockActual(producto.getStockActual() - item.getCantidad());
            productoRepo.save(producto);

            // Crea el detalle — @PrePersist calculará mes/semana automáticamente
            VentaDetalle detalle = VentaDetalle.builder()
                    .producto(producto)
                    .venta(venta)
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .fechaVenta(req.getFechaVenta())
                    .build();

            totalVenta = totalVenta.add(
                    producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()))
            );
            detalles.add(detalle);
        }

        // 4. Guarda venta con total calculado
        venta.setTotal(totalVenta);
        venta.setDetalles(detalles);
        return ventaRepo.save(venta);
    }
}