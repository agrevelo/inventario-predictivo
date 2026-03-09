package com.inventario.backend.service;

import com.inventario.backend.dto.DashboardDTO;
import com.inventario.backend.dto.DashboardDTO.GraficaVentasDTO;
import com.inventario.backend.repository.ProductoRepository;
import com.inventario.backend.repository.VentaDetalleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepo;
    private final VentaDetalleRepository detalleRepo;
    private final ProductoService productoService;

    public DashboardDTO getDashboard() {
        LocalDate hoy = LocalDate.now();

        // Genera la gráfica de ventas de los últimos 6 meses
        List<GraficaVentasDTO> grafica = generarGraficaVentas(hoy);

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
                .graficaVentas(grafica)
                .build();
    }

    // Genera datos de los últimos 6 meses para la gráfica del frontend
    private List<GraficaVentasDTO> generarGraficaVentas(LocalDate hoy) {
        List<GraficaVentasDTO> resultado = new ArrayList<>();
        Locale locale = new Locale("es");

        for (int i = 5; i >= 0; i--) {
            LocalDate fecha = hoy.minusMonths(i);
            int mes = fecha.getMonthValue();
            int anio = fecha.getYear();
            String nombreMes = Month.of(mes).getDisplayName(TextStyle.SHORT, locale)
                    + " " + anio;

            Long ventas = detalleRepo.countVentasMes(mes, anio);

            resultado.add(GraficaVentasDTO.builder()
                    .mes(nombreMes)
                    .ventasReales(ventas)
                    .prediccion(null) // null para meses pasados, se llena en el endpoint de predicción
                    .build());
        }
        return resultado;
    }
}