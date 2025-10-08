package com.java2.tpi2SQLite;

import com.java2.tpi2SQLite.auth.AuthController;
import com.java2.tpi2SQLite.auth.AuthService;
import com.java2.tpi2SQLite.auth.dto.AuthRequest;
import com.java2.tpi2SQLite.auth.dto.AuthResponse;
import com.java2.tpi2SQLite.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
    }

    @Test
    void testRegister() {
        RegisterRequest request = new RegisterRequest();
        AuthResponse response = new AuthResponse("token");
        when(authService.register(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.register(request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("token", result.getBody().getToken());
    }

    @Test
    void testLoginSuccess() {
        AuthRequest request = new AuthRequest();
        AuthResponse response = new AuthResponse("token");
        when(authService.authenticate(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.login(request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("token", result.getBody().getToken());
    }

    @Test
    void testLoginUnauthorized() {
        AuthRequest request = new AuthRequest();
        when(authService.authenticate(request)).thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<AuthResponse> result = authController.login(request);

        assertEquals(401, result.getStatusCodeValue());
        assertNull(result.getBody());
    }
}
