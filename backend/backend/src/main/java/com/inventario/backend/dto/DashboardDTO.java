package com.inventario.backend.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    private Integer totalProductos;
    private Integer productosStockCritico;   // stock <= stockMinimo
    private Integer productosStockBajo;      // stock <= puntoReorden
    private Long totalVentasHoy;
    private Long totalVentasMes;
    private List<ProductoDTO> alertasCriticas;
    private List<GraficaVentasDTO> graficaVentas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GraficaVentasDTO {
        private String mes;
        private Long ventasReales;
        private Long prediccion;   // se llenará con la IA en el Paso 5
    }
}