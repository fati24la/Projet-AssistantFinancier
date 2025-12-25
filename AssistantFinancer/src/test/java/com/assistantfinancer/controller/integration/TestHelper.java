package com.assistantfinancer.controller.integration;

import com.assistantfinancer.config.JwtService;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public User createTestUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public String generateTokenForUser(User user) {
        return jwtService.generateToken(user.getUsername());
    }

    public String getAuthHeader(String token) {
        return "Bearer " + token;
    }
}

