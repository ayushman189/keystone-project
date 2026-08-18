package com.keystone.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.keystone.backend.entity.User;
import com.keystone.backend.repository.UserRepository;
import com.keystone.backend.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AuthenticatedControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JwtService jwtService;

    protected User createUser(String email, String role) {
        User user = new User();
        user.setName(role + " Test");
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        return userRepository.save(user);
    }

    protected String tokenFor(User user) {
        return jwtService.generateToken(user.getEmail(), user.getRole());
    }
}
