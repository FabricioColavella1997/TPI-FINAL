package com.java2.tpi2SQLite;
import com.java2.tpi2SQLite.auth.jwt.JwtService;
import com.java2.tpi2SQLite.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        java.lang.reflect.Field secretField;
        try {
            secretField = JwtService.class.getDeclaredField("jwtSecret");
            secretField.setAccessible(true);
            secretField.set(jwtService, "testsecretkeytestsecretkeytestsecretkey");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole("USER");
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        String token = jwtService.generateToken(user, Duration.ofMinutes(10));
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testBuildClaims() {
        var claims = jwtService.buildClaims(user);
        assertEquals(1L, claims.get("id"));
        assertEquals("testuser", claims.get("username"));
        assertEquals("USER", claims.get("role"));
    }

    @Test
    void testIsTokenValid() {
        String token = jwtService.generateToken(user, Duration.ofMinutes(10));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void testGetIsoExpirationFromToken() {
        String token = jwtService.generateToken(user, Duration.ofMinutes(10));
        String isoExp = jwtService.getIsoExpirationFromToken(token);
        assertNotNull(isoExp);
        assertTrue(isoExp.endsWith("Z"));
    }
}