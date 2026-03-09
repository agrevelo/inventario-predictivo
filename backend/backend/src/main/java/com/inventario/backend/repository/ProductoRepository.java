package com.inventario.backend.repository;

import com.inventario.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    // Spring genera: SELECT * FROM productos WHERE activo = true
    List<Producto> findByActivoTrue();

    // Spring genera: SELECT * FROM productos WHERE nombre ILIKE '%texto%'
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Productos en stock crítico (para alertas del dashboard)
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo AND p.activo = true")
    List<Producto> findProductosStockCritico();

    // Productos por debajo del punto de reorden (alerta temprana)
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.puntoReorden AND p.activo = true")
    List<Producto> findProductosStockBajo();

    // Buscar por categoría
    List<Producto> findByCategoriaId(String categoriaId);

    // Contar productos activos
    Long countByActivoTrue();
}