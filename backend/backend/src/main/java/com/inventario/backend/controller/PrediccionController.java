package com.inventario.backend.controller;

import com.inventario.backend.dto.PrediccionDTO;
import com.inventario.backend.service.PrediccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/predicciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrediccionController {

    private final PrediccionService prediccionService;

    // GET /api/v1/predicciones/{productoId}
    // Genera y retorna la predicción de demanda para un producto específico
    @GetMapping("/{productoId}")
    public ResponseEntity<PrediccionDTO> predecir(
            @PathVariable String productoId) {
        return ResponseEntity.ok(prediccionService.predecirDemanda(productoId));
    }

    // GET /api/v1/predicciones/producto/{productoId}/dashboard
    // Versión ligera para el widget del dashboard (sin proyección completa)
    @GetMapping("/producto/{productoId}/resumen")
    public ResponseEntity<PrediccionDTO> resumen(
            @PathVariable String productoId) {
        PrediccionDTO prediccion = prediccionService.predecirDemanda(productoId);
        // Retorna solo los campos esenciales para el widget
        return ResponseEntity.ok(PrediccionDTO.builder()
                .productoId(prediccion.getProductoId())
                .productoNombre(prediccion.getProductoNombre())
                .prediccionProximoMes(prediccion.getPrediccionProximoMes())
                .tendencia(prediccion.getTendencia())
                .stockSugerido(prediccion.getStockSugerido())
                .confianza(prediccion.getConfianza())
                .alertas(prediccion.getAlertas())
                .tieneSuficienteHistorial(prediccion.getTieneSuficienteHistorial())
                .build());
    }
}