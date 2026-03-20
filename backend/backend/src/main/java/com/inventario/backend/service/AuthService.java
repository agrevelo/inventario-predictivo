package com.inventario.backend.service;

import com.inventario.backend.dto.*;
import com.inventario.backend.model.Usuario;
import com.inventario.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    // ── REGISTRO ──────────────────────────────────────────────────────────
    public AuthResponse registrar(RegisterRequest req) {

        if (usuarioRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Ya existe una cuenta con el email: " + req.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .nombre(req.getNombre())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword())) // BCrypt aquí
                .empresa(req.getEmpresa())
                .rol(Usuario.Rol.USER)
                .build();

        usuarioRepo.save(usuario);

        String token = jwtService.generarToken(usuario);
        return construirRespuesta(usuario, token);
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────
    public AuthResponse login(AuthRequest req) {

        // AuthenticationManager valida email + password automáticamente
        // Lanza excepción si las credenciales son incorrectas
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        Usuario usuario = usuarioRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generarToken(usuario);
        return construirRespuesta(usuario, token);
    }

    private AuthResponse construirRespuesta(Usuario u, String token) {
        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .email(u.getEmail())
                .nombre(u.getNombre())
                .rol(u.getRol().name())
                .expiracionMs(jwtService.getExpiration())
                .build();
    }
}