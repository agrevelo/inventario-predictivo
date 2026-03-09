package com.inventario.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequest {

    @NotNull
    private LocalDate fechaVenta;

    private String numeroFactura;

    @NotEmpty(message = "La venta debe tener al menos un producto")
    private List<VentaDetalleRequest> detalles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VentaDetalleRequest {
        @NotBlank
        private String productoId;

        @Min(1)
        private Integer cantidad;
    }
}