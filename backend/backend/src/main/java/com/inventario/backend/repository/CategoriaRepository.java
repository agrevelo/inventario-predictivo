package com.inventario.backend.repository;

import com.inventario.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, String> {
    Optional<Categoria> findByNombre(String nombre);
    Boolean existsByNombre(String nombre);
}