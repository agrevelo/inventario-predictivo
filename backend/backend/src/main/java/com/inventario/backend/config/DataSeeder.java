package com.inventario.backend.config;

import com.inventario.backend.model.*;
import com.inventario.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    // CommandLineRunner: corre automáticamente al iniciar Spring Boot

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;
    private final VentaRepository ventaRepo;
    private final VentaDetalleRepository detalleRepo;

    @Override
    @Transactional
    public void run(String... args) {
        // Solo ejecuta si la BD está vacía
        if (productoRepo.count() > 0) {
            log.info("DataSeeder: datos ya existen, omitiendo carga inicial");
            return;
        }

        log.info("DataSeeder: cargando datos de prueba...");

        // 1. Crear categorías
        Categoria electronica = categoriaRepo.save(Categoria.builder()
                .nombre("Electrónica").descripcion("Dispositivos electrónicos").build());
        Categoria oficina = categoriaRepo.save(Categoria.builder()
                .nombre("Oficina").descripcion("Suministros de oficina").build());

        // 2. Crear productos
        Producto laptop = productoRepo.save(Producto.builder()
                .nombre("Laptop Dell XPS 15")
                .sku("DELL-XPS-15")
                .precio(new BigDecimal("1299.99"))
                .stockActual(15)
                .stockMinimo(3)
                .puntoReorden(6)
                .categoria(electronica)
                .activo(true)
                .build());

        Producto mouse = productoRepo.save(Producto.builder()
                .nombre("Mouse Inalámbrico Logitech")
                .sku("LOG-MOUSE-WL")
                .precio(new BigDecimal("45.99"))
                .stockActual(50)
                .stockMinimo(10)
                .puntoReorden(20)
                .categoria(electronica)
                .activo(true)
                .build());

        Producto resma = productoRepo.save(Producto.builder()
                .nombre("Resma Papel A4")
                .sku("PAPEL-A4-500")
                .precio(new BigDecimal("4.50"))
                .stockActual(200)
                .stockMinimo(50)
                .puntoReorden(100)
                .categoria(oficina)
                .activo(true)
                .build());

        // 3. Crear historial de ventas con patrón estacional realista
        // Ventas mensuales: [ene, feb, mar, abr, may, jun, jul, ago, sep, oct, nov, dic]
        int[] ventasLaptop = {3, 2, 4, 5, 4, 3, 5, 6, 7, 8, 10, 15};  // pico en dic (Navidad)
        int[] ventasMouse  = {8, 6, 9, 10, 9, 8, 11, 12, 14, 15, 18, 25}; // también crece en dic
        int[] ventasResma  = {40, 35, 45, 50, 48, 40, 42, 55, 60, 58, 65, 45}; // pico en sep (inicio clases)

        int anioBase = LocalDate.now().getYear() - 1; // año pasado

        for (int mes = 1; mes <= 12; mes++) {
            LocalDate fecha = LocalDate.of(anioBase, mes, 15);

            crearVentaConDetalle(laptop, ventasLaptop[mes - 1], fecha);
            crearVentaConDetalle(mouse,  ventasMouse[mes - 1],  fecha);
            crearVentaConDetalle(resma,  ventasResma[mes - 1],  fecha);
        }

        log.info("DataSeeder: {} productos y 36 registros de ventas cargados ✓",
                productoRepo.count());
    }

    private void crearVentaConDetalle(Producto p, int cantidad, LocalDate fecha) {
        Venta venta = ventaRepo.save(Venta.builder()
                .fechaVenta(fecha)
                .total(p.getPrecio().multiply(new BigDecimal(cantidad)))
                .build());

        detalleRepo.save(VentaDetalle.builder()
                .producto(p)
                .venta(venta)
                .cantidad(cantidad)
                .precioUnitario(p.getPrecio())
                .fechaVenta(fecha)
                .build());
    }
}