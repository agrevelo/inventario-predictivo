package com.inventario.backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;  // 86400000 ms = 24 horas

    // ── Generar token ─────────────────────────────────────────────────────
    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Agrega el rol como claim extra en el token
        claims.put("rol", userDetails.getAuthorities().iterator().next().getAuthority());
        return generarToken(claims, userDetails);
    }

    private String generarToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())      // email del usuario
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSecretKey())                // firma con HMAC-SHA256
                .compact();
    }

    // ── Validar token ─────────────────────────────────────────────────────
    public boolean esTokenValido(String token, UserDetails userDetails) {
        final String email = extraerEmail(token);
        // Válido si el email coincide Y el token no expiró
        return email.equals(userDetails.getUsername()) && !estaExpirado(token);
    }

    // ── Extraer datos del token ───────────────────────────────────────────
    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return resolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean estaExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    // Convierte el JWT_SECRET (string) a una clave criptográfica segura
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getExpiration() {
        return jwtExpiration;
    }
}