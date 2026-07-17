package com.careercompass.service;

import com.careercompass.config.JwtProperties;
import com.careercompass.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return generateToken(user, ACCESS_TOKEN_TYPE, Duration.ofMinutes(properties.accessTokenMinutes()));
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_TOKEN_TYPE, Duration.ofDays(properties.refreshTokenDays()));
    }

    public long accessTokenSeconds() {
        return Duration.ofMinutes(properties.accessTokenMinutes()).toSeconds();
    }

    public Optional<UUID> extractAccessUserId(String token) {
        return extractUserId(token, ACCESS_TOKEN_TYPE);
    }

    public Optional<UUID> extractRefreshUserId(String token) {
        return extractUserId(token, REFRESH_TOKEN_TYPE);
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, ACCESS_TOKEN_TYPE);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, REFRESH_TOKEN_TYPE);
    }

    private String generateToken(User user, String tokenType, Duration ttl) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ttl);
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    private Optional<UUID> extractUserId(String token, String expectedTokenType) {
        try {
            Claims claims = parseClaims(token);
            if (!expectedTokenType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                return Optional.empty();
            }
            return Optional.of(UUID.fromString(claims.getSubject()));
        } catch (IllegalArgumentException | JwtException exception) {
            return Optional.empty();
        }
    }

    private boolean isTokenValid(String token, UserDetails userDetails, String expectedTokenType) {
        try {
            Claims claims = parseClaims(token);
            return expectedTokenType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                    && userDetails.getUsername().equalsIgnoreCase(claims.get("email", String.class))
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
