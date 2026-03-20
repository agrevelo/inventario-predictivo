package com.inventario.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;  // siempre guardado con BCrypt, NUNCA en texto plano

    private String nombre;
    private String empresa;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    private Boolean activo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (activo == null) activo = true;
        if (rol == null) rol = Rol.USER;
    }

    // ── Métodos de UserDetails ────────────────────────────────────────────
    // Spring Security llama a estos métodos para verificar permisos

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convierte el rol a formato que Spring Security entiende
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getUsername() {
        return email; // usamos email como username
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return activo; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return activo; }

    // Enum interno para los roles del sistema
    public enum Rol {
        ADMIN,  // acceso total
        USER    // acceso básico (ver datos, no administrar)
    }
}