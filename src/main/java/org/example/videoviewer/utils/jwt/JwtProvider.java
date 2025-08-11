package org.example.videoviewer.utils.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.example.videoviewer.repositories.model.Users;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {
    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;

    public JwtProvider(
            @Value("${application.secrets.access}") String accessSecret,
            @Value("${application.secrets.refresh}") String refreshSecret) {
        this.accessSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    }

    public String generateAccessToken(final Users user) {
        var now = LocalDateTime.now();
        var expirationInstant = now.plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant();
        var expirationDate = Date.from(expirationInstant);

        return Jwts.builder()
                .subject(user.getUsername())
                .expiration(expirationDate)
                .claim("roles", user.getRoles())
                .signWith(accessSecretKey)
                .compact();
    }

    public String generateRefreshToken(final Users user) {
        var now = LocalDateTime.now();
        var expirationInstant = now.plusHours(1).atZone(ZoneId.systemDefault()).toInstant();
        var expirationDate = Date.from(expirationInstant);

        return Jwts.builder()
                .subject(user.getUsername())
                .expiration(expirationDate)
                .signWith(refreshSecretKey)
                .compact();
    }

    public boolean validateAccessToken(@NonNull final String token) {
        return validateToken(token, accessSecretKey);
    }

    public boolean validateRefreshToken(@NonNull final String token) {
        return validateToken(token, refreshSecretKey);
    }

    private boolean validateToken(@NonNull final String token, final SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public Claims getAccessClaims(@NonNull final String token) {
        return getClaims(token, accessSecretKey);
    }

    public Claims getRefreshClaims(@NonNull final String token) {
        return getClaims(token, refreshSecretKey);
    }

    private Claims getClaims(@NonNull final String token, final SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
