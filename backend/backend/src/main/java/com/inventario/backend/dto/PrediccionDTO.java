package com.inventario.backend.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrediccionDTO {

    private String productoId;
    private String productoNombre;

    // Predicción numérica
    private Integer prediccionProximoMes;    // unidades estimadas a vender
    private Integer stockSugerido;            // cuánto pedir para cubrir demanda
    private Integer confianza;               // 0-100, qué tan seguro está Gemini

    // Análisis cualitativo
    private String tendencia;               // "CRECIENTE", "ESTABLE", "DECRECIENTE"
    private String estacionalidad;          // "ALTA", "MEDIA", "BAJA"
    private String razonamiento;            // explicación en español de la predicción

    // Alertas y acciones sugeridas
    private List<String> alertas;           // ej: ["Temporada alta en diciembre detectada"]
    private List<String> accionesSugeridas; // ej: ["Ordenar 50 unidades esta semana"]

    // Proyección mensual para la gráfica del dashboard
    private List<ProyeccionMensual> proyeccion3Meses;

    // Estado del análisis
    private Boolean tieneSuficienteHistorial; // false si hay menos de 3 meses de datos
    private String mensaje;                  // mensaje para mostrar al usuario

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProyeccionMensual {
        private String mes;           // "Abril 2026"
        private Integer unidades;     // unidades proyectadas
        private String nivel;        // "BAJO", "NORMAL", "ALTO"
    }
}