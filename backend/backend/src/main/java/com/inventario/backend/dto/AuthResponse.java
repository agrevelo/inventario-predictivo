package com.inventario.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;         // el JWT que el cliente debe guardar
    private String tipo;          // siempre "Bearer"
    private String email;
    private String nombre;
    private String rol;
    private Long expiracionMs;    // cuándo expira el token (para el frontend)
}