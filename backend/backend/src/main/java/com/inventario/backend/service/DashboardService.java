package com.inventario.backend.service;

import com.inventario.backend.dto.DashboardDTO;
import com.inventario.backend.repository.ProductoRepository;
import com.inventario.backend.repository.VentaDetalleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepo;
    private final VentaDetalleRepository detalleRepo;
    private final ProductoService productoService;

    public DashboardDTO getDashboard() {
        LocalDate hoy = LocalDate.now();

        // Construye el resumen ejecutivo
        return DashboardDTO.builder()
                .totalProductos(productoRepo.countByActivoTrue().intValue())
                .productosStockCritico(productoRepo.findProductosStockCritico().size())
                .productosStockBajo(productoRepo.findProductosStockBajo().size())
                .totalVentasHoy(detalleRepo.countVentasHoy())
                .totalVentasMes(detalleRepo.countVentasMes(hoy.getMonthValue(), hoy.getYear()))
                .alertasCriticas(
                        productoRepo.findProductosStockCritico()
                                .stream()
                                .map(productoService::toDTO)
                                .collect(Collectors.toList())
                )
                .graficaVentas(List.of()) // se llenará con IA en el Paso 5
                .build();
    }
}