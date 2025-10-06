package com.java2.tpi2SQLite.auth.jwt;

import com.java2.tpi2SQLite.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-minutes}")
    private long defaultExpirationMinutes;

    public String generateToken(User user, Duration expiration) {
        Map<String, Object> claims = buildClaims(user);
        Instant now = Instant.now();
        Instant expInstant = now.plus(expiration);
        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expInstant))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, Object> buildClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        return claims;
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        return username.equals(user.getUsername()) && expiration.after(new Date());
    }

    public String getIsoExpirationFromToken(String token) {
        Claims claims = extractAllClaims(token);
        Date exp = claims.getExpiration();
        return DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(exp.toInstant());
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


