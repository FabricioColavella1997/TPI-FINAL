package com.java2.tpi2SQLite;
import com.java2.tpi2SQLite.auth.jwt.JwtAuthFilter;
import com.java2.tpi2SQLite.auth.jwt.JwtService;
import com.java2.tpi2SQLite.domain.User;
import com.java2.tpi2SQLite.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private JwtAuthFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        filter = new JwtAuthFilter(jwtService, userRepository);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }
    @Test
    void testFilterAllowsLoginAndRegisterPaths() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/login");
        filter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);

        when(request.getServletPath()).thenReturn("/register");
        filter.doFilter(request, response, filterChain);
        verify(filterChain, times(2)).doFilter(request, response);
    }
    @Test
    void testFilterSkipsIfNoAuthHeader() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/other");
        when(request.getHeader("Authorization")).thenReturn(null);
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterSkipsIfAuthHeaderNotBearer() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/other");
        when(request.getHeader("Authorization")).thenReturn("Basic xyz");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterSkipsIfJwtServiceThrows() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.extractUsername("token")).thenThrow(new RuntimeException("Invalid token"));
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterAuthenticatesValidToken() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.extractUsername("token")).thenReturn("user1");
        User user = new User();
        user.setUsername("user1");
        user.setPassword("pass");
        user.setRole("admin");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("token", user)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
