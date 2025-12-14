package com.assistantfinancer.controller;

import com.assistantfinancer.config.JwtService;
import com.assistantfinancer.dto.LoginRequest;
import com.assistantfinancer.dto.RegisterRequest;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("🔐 [AuthController] Tentative de connexion pour: " + request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null) {
            System.out.println("❌ [AuthController] Utilisateur non trouvé: " + request.getUsername());
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }

        System.out.println("✅ [AuthController] Utilisateur trouvé: " + user.getUsername());
        System.out.println("🔑 [AuthController] Vérification du mot de passe...");

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        
        if (!passwordMatches) {
            System.out.println("❌ [AuthController] Mot de passe incorrect pour: " + request.getUsername());
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }

        System.out.println("✅ [AuthController] Mot de passe correct, génération du token...");
        String token = jwtService.generateToken(user.getUsername());
        System.out.println("✅ [AuthController] Token généré avec succès pour: " + user.getUsername());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "userId", user.getId()
        ));
    }
}

