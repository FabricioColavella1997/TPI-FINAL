package com.java2.tpi2SQLite;

import com.java2.tpi2SQLite.auth.AuthService;
import com.java2.tpi2SQLite.auth.dto.AuthRequest;
import com.java2.tpi2SQLite.auth.dto.AuthResponse;
import com.java2.tpi2SQLite.auth.dto.RegisterRequest;
import com.java2.tpi2SQLite.auth.jwt.JwtService;
import com.java2.tpi2SQLite.domain.User;
import com.java2.tpi2SQLite.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);

        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
        // Reflejar el valor de expirationMinutes si es necesario
        // Puedes usar ReflectionTestUtils si usas Spring Test
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setFullname("Juan Perez");
        request.setUsername("juan");
        request.setPassword("1234");

        when(userRepository.existsByUsername("juan")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("hashed");
        when(jwtService.generateToken(any(User.class), any(Duration.class))).thenReturn("token123");
        when(jwtService.getIsoExpirationFromToken("token123")).thenReturn("2024-12-31T23:59:59Z");

        AuthResponse response = authService.register(request);

        assertEquals("token123", response.getToken());
        assertEquals("2024-12-31T23:59:59Z", response.getExpiresAt());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("Juan Perez", savedUser.getFullname());
        assertEquals("juan", savedUser.getUsername());
        assertEquals("hashed", savedUser.getPassword());
        assertEquals("user", savedUser.getRole());
    }

    @Test
    void testRegisterUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("juan");

        when(userRepository.existsByUsername("juan")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void testAuthenticateSuccess() {
        AuthRequest request = new AuthRequest();
        request.setUsername("juan");
        request.setPassword("1234");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        User user = new User();
        user.setUsername("juan");
        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class), any(Duration.class))).thenReturn("token456");
        when(jwtService.getIsoExpirationFromToken("token456")).thenReturn("2024-12-31T23:59:59Z");

        AuthResponse response = authService.authenticate(request);

        assertEquals("token456", response.getToken());
        assertEquals("2024-12-31T23:59:59Z", response.getExpiresAt());
    }

    @Test
    void testAuthenticateUserNotFound() {
        AuthRequest request = new AuthRequest();
        request.setUsername("juan");
        request.setPassword("1234");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByUsername("juan")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.authenticate(request));
    }

    @Test
    void testAuthenticateException() {
        AuthRequest request = new AuthRequest();
        request.setUsername("juan");
        request.setPassword("1234");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(RuntimeException.class, () -> authService.authenticate(request));
    }
}