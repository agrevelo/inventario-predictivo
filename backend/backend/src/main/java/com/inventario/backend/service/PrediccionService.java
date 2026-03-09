package com.inventario.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventario.backend.dto.PrediccionDTO;
import com.inventario.backend.model.Producto;
import com.inventario.backend.model.VentaDetalle;
import com.inventario.backend.repository.ProductoRepository;
import com.inventario.backend.repository.VentaDetalleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j  // habilita log.info(), log.error(), etc.
public class PrediccionService {

    private final ChatClient chatClient;             // Spring AI — conexión a Gemini
    private final ProductoRepository productoRepo;
    private final VentaDetalleRepository detalleRepo;
    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper;          // para parsear JSON de Gemini

    public PrediccionDTO predecirDemanda(String productoId) {

        // 1. Buscar el producto
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        // 2. Obtener historial de los últimos 2 años
        int anioDesde = LocalDate.now().getYear() - 2;
        List<VentaDetalle> historial = detalleRepo.findHistorialProducto(productoId, anioDesde);

        // 3. Si no hay suficiente historial, retornar respuesta básica
        if (historial.size() < 3) {
            return PrediccionDTO.builder()
                    .productoId(productoId)
                    .productoNombre(producto.getNombre())
                    .tieneSuficienteHistorial(false)
                    .mensaje("Se necesitan al menos 3 registros de ventas para generar una predicción confiable.")
                    .alertas(List.of("Historial insuficiente — registra más ventas para activar la IA"))
                    .build();
        }

        // 4. Construir el resumen del historial para el prompt
        String resumenHistorial = construirResumenHistorial(historial);
        String mesActual = LocalDate.now().getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("es"));

        // 5. Construir el prompt — esto es lo que recibe Gemini
        String prompt = construirPrompt(producto, resumenHistorial, mesActual);

        // 6. Llamar a Gemini vía Spring AI
        log.info("Llamando a Gemini para predicción de producto: {}", producto.getNombre());
        String respuestaJson = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 7. Parsear la respuesta JSON de Gemini
        PrediccionDTO prediccion = parsearRespuesta(respuestaJson, productoId, producto.getNombre());

        // 8. Guardar el stock sugerido en el producto para el dashboard
        if (prediccion.getStockSugerido() != null) {
            producto.setStockSugeridoIa(prediccion.getStockSugerido());
            productoRepository.save(producto);
            log.info("Stock sugerido actualizado para {}: {}",
                    producto.getNombre(), prediccion.getStockSugerido());
        }

        return prediccion;
    }

    // ── Construye el resumen del historial agrupado por mes ──────────────
    private String construirResumenHistorial(List<VentaDetalle> historial) {
        // Agrupa las ventas por "YYYY-MM" y suma cantidades
        Map<String, Integer> ventasPorMes = new LinkedHashMap<>();

        for (VentaDetalle vd : historial) {
            String clave = vd.getAnio() + "-" +
                    String.format("%02d", vd.getMes()); // ej: "2025-03"
            ventasPorMes.merge(clave, vd.getCantidad(), Integer::sum);
        }

        // Convierte a texto legible para el prompt
        StringBuilder sb = new StringBuilder();
        ventasPorMes.forEach((mes, cantidad) ->
                sb.append(String.format("  - %s: %d unidades vendidas\n", mes, cantidad))
        );
        return sb.toString();
    }

    // ── Construye el prompt que le enviamos a Gemini ─────────────────────
    private String construirPrompt(Producto p, String historial, String mesActual) {
        return """
        Eres un experto analista de inventarios para empresas en Ecuador.
        Analiza el siguiente historial de ventas y genera una predicción precisa.

        PRODUCTO: %s
        STOCK ACTUAL: %d unidades
        STOCK MÍNIMO: %d unidades
        MES ACTUAL: %s

        HISTORIAL DE VENTAS (formato YYYY-MM: cantidad):
        %s

        REGLAS ESTRICTAS — DEBES SEGUIRLAS EXACTAMENTE:
        1. "confianza" DEBE ser un número entero entre 0 y 100. NUNCA texto como "Baja" o "Alta".
        2. "tendencia" DEBE ser exactamente una de estas palabras: CRECIENTE, ESTABLE, DECRECIENTE
        3. "estacionalidad" DEBE ser exactamente una de estas palabras: ALTA, MEDIA, BAJA
        4. "nivel" en proyeccion3Meses DEBE ser exactamente una de estas palabras: BAJO, NORMAL, ALTO
        5. "razonamiento" DEBE ser máximo 2 oraciones cortas.
        6. Tu respuesta DEBE ser ÚNICAMENTE el objeto JSON. Sin texto antes, sin texto después.
        7. NUNCA uses bloques markdown ni triple backtick.

        Usa EXACTAMENTE esta estructura JSON:
        {
          "prediccionProximoMes": <entero>,
          "stockSugerido": <entero>,
          "confianza": <entero 0-100>,
          "tendencia": "<CRECIENTE|ESTABLE|DECRECIENTE>",
          "estacionalidad": "<ALTA|MEDIA|BAJA>",
          "razonamiento": "<máximo 2 oraciones>",
          "alertas": ["<alerta1>", "<alerta2>"],
          "accionesSugeridas": ["<accion1>", "<accion2>"],
          "proyeccion3Meses": [
            {"mes": "<Mes Año>", "unidades": <entero>, "nivel": "<BAJO|NORMAL|ALTO>"},
            {"mes": "<Mes Año>", "unidades": <entero>, "nivel": "<BAJO|NORMAL|ALTO>"},
            {"mes": "<Mes Año>", "unidades": <entero>, "nivel": "<BAJO|NORMAL|ALTO>"}
          ],
          "tieneSuficienteHistorial": true
        }
        """.formatted(
                p.getNombre(),
                p.getStockActual(),
                p.getStockMinimo(),
                mesActual,
                historial
        );
    }

    // ── Parsea la respuesta JSON de Gemini a PrediccionDTO ───────────────
    private PrediccionDTO parsearRespuesta(String json, String productoId, String nombre) {
        try {
            // Limpia posibles bloques markdown que Gemini a veces agrega
            String jsonLimpio = json
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            PrediccionDTO dto = objectMapper.readValue(jsonLimpio, PrediccionDTO.class);
            dto.setProductoId(productoId);
            dto.setProductoNombre(nombre);
            dto.setMensaje("Predicción generada exitosamente por Gemini 1.5 Flash");
            return dto;

        } catch (Exception e) {
            // Si Gemini devuelve algo inesperado, retornamos un error manejado
            log.error("Error parseando respuesta de Gemini: {}", e.getMessage());
            log.debug("Respuesta raw de Gemini: {}", json);

            return PrediccionDTO.builder()
                    .productoId(productoId)
                    .productoNombre(nombre)
                    .tieneSuficienteHistorial(true)
                    .confianza(0)
                    .mensaje("Error al procesar la respuesta de IA. Intenta nuevamente.")
                    .alertas(List.of("Error temporal en el servicio de IA"))
                    .build();
        }
    }
}