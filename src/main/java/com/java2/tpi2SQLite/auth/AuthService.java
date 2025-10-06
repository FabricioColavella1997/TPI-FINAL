package com.java2.tpi2SQLite.auth;


import com.java2.tpi2SQLite.auth.dto.AuthRequest;
import com.java2.tpi2SQLite.auth.dto.AuthResponse;
import com.java2.tpi2SQLite.auth.dto.RegisterRequest;
import com.java2.tpi2SQLite.auth.jwt.JwtService;
import com.java2.tpi2SQLite.domain.User;
import com.java2.tpi2SQLite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setFullname(request.getFullname());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        userRepository.save(user);

        String token = jwtService.generateToken(user, Duration.ofMinutes(expirationMinutes));
        String expiresAt = jwtService.getIsoExpirationFromToken(token);
        return new AuthResponse(token, expiresAt);
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw ex;
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtService.generateToken(user, Duration.ofMinutes(expirationMinutes));
        String expiresAt = jwtService.getIsoExpirationFromToken(token);
        return new AuthResponse(token, expiresAt);
    }
}


