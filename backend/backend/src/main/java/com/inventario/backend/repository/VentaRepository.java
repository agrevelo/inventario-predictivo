package com.inventario.backend.repository;

import com.inventario.backend.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, String> {
    List<Venta> findByFechaVentaBetween(LocalDate inicio, LocalDate fin);
    Boolean existsByNumeroFactura(String numeroFactura);
}