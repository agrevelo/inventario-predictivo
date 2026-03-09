package com.inventario.backend.repository;

import com.inventario.backend.model.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, String> {

    // Historial de ventas de un producto por año — para detectar estacionalidad
    @Query("""
        SELECT vd FROM VentaDetalle vd
        WHERE vd.producto.id = :productoId
        AND vd.anio >= :anioDesde
        ORDER BY vd.anio ASC, vd.mes ASC
        """)
    List<VentaDetalle> findHistorialProducto(
            @Param("productoId") String productoId,
            @Param("anioDesde") Integer anioDesde
    );

    // Ventas agrupadas por mes — para la gráfica del dashboard
    @Query("""
        SELECT vd.anio, vd.mes, SUM(vd.cantidad)
        FROM VentaDetalle vd
        WHERE vd.producto.id = :productoId
        GROUP BY vd.anio, vd.mes
        ORDER BY vd.anio ASC, vd.mes ASC
        """)
    List<Object[]> findVentasMensuales(@Param("productoId") String productoId);

    // Total de ventas del mes actual (para el dashboard general)
    @Query("""
        SELECT COUNT(vd) FROM VentaDetalle vd
        WHERE vd.mes = :mes AND vd.anio = :anio
        """)
    Long countVentasMes(
            @Param("mes") Integer mes,
            @Param("anio") Integer anio
    );

    // Total ventas de hoy
    @Query("""
        SELECT COUNT(vd) FROM VentaDetalle vd
        WHERE vd.fechaVenta = CURRENT_DATE
        """)
    Long countVentasHoy();
}