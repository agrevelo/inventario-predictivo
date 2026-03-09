package com.inventario.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {
    private String id;
    private String nombre;
    private String descripcion;
    private String sku;
    private BigDecimal precio;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer puntoReorden;
    private Integer stockSugeridoIa;
    private String categoriaNombre;   // solo el nombre, no el objeto completo
    private String categoriaId;
    private Boolean activo;
    private LocalDateTime createdAt;
    private String estadoStock;       // "CRITICO", "BAJO", "NORMAL", "ALTO"
}