package com.keystone.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.keystone.backend.dto.LoginResponse;
import com.keystone.backend.entity.User;
import com.keystone.backend.repository.UserRepository;
import com.keystone.backend.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return new LoginResponse(token, user.getRole());
    }
}