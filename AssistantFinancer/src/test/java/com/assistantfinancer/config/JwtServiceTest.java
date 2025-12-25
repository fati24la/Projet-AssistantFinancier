package com.assistantfinancer.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        // Given
        String username = "testuser";

        // When
        String token = jwtService.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_ShouldContainUsername() {
        // Given
        String username = "testuser";

        // When
        String token = jwtService.generateToken(username);
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void generateToken_ShouldExpireAfter24Hours() {
        // Given
        String username = "testuser";

        // When
        String token = jwtService.generateToken(username);

        // Then
        assertNotNull(token);
        // Verify expiration by checking claims
        try {
            String SECRET = "super_secret_key_change_this_please_123456";
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            assertNotNull(expiration);
            // Expiration should be approximately 24 hours from now
            long expectedExpiration = System.currentTimeMillis() + 86400000; // 24h in ms
            long actualExpiration = expiration.getTime();
            // Allow 5 seconds tolerance
            assertTrue(Math.abs(expectedExpiration - actualExpiration) < 5000);
        } catch (Exception e) {
            fail("Token should be valid");
        }
    }

    @Test
    void extractUsername_ShouldExtractUsernameFromToken() {
        // Given
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void extractUsername_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(invalidToken);
        });
    }

    @Test
    void extractUsername_WithExpiredToken_ShouldThrowException() {
        // Given
        String SECRET = "super_secret_key_change_this_please_123456";
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        
        // Create an expired token
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 86400000)) // 24h ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1h ago
                .signWith(key)
                .compact();

        // When & Then
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.extractUsername(expiredToken);
        });
    }
}

