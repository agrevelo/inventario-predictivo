package com.inventario.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Entity
@Table(name = "ventas_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "fecha_venta")
    private LocalDate fechaVenta;

    // Campos para análisis de estacionalidad — la IA los usa para detectar patrones
    @Column(name = "anio")
    private Integer anio;

    @Column(name = "mes")
    private Integer mes;

    @Column(name = "semana")
    private Integer semana;

    @Column(name = "dia_semana")
    private Integer diaSemana;

    @PrePersist
    protected void calcularCampos() {
        // Calcula automáticamente los campos de tiempo al guardar
        if (fechaVenta != null) {
            anio     = fechaVenta.getYear();
            mes      = fechaVenta.getMonthValue();
            semana   = fechaVenta.get(WeekFields.of(Locale.getDefault()).weekOfYear());
            diaSemana = fechaVenta.getDayOfWeek().getValue();
        }
        // Calcula el subtotal automáticamente
        if (cantidad != null && precioUnitario != null) {
            subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }
}