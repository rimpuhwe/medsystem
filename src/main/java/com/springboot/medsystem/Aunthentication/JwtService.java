package com.springboot.medsystem.Aunthentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
public class JwtService {

    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60;

    private final Key key;

    public JwtService() {
        String secret = System.getenv("JWT_SECRET");

        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 characters"
            );
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Role not found"));

        return Jwts.builder()
                .claim("role", role) // keep ROLE_ prefix
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    /* ---------- FOR OAUTH2 LOGIN ---------- */
    public String generateTokenFromOAuth(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        String role = oAuth2User.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new IllegalStateException("Role missing"));

        return Jwts.builder()
                .claim("role", role) // MUST be ROLE_PATIENT / ROLE_PHARMACIST
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    /* ---------- SHARED TOKEN BUILDER ---------- */
    private String buildToken(String subject, Object roles) {
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(Map.of("roles", roles))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }


    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
