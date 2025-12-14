package com.assistantfinancer.config;

import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si l'utilisateur admin existe déjà
        if (userRepository.findByUsername("admin").isPresent()) {
            System.out.println("✅ L'utilisateur admin existe déjà.");
            return;
        }

        System.out.println("🔐 Création de l'utilisateur admin par défaut...");

        // Créer l'utilisateur admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@assistantfinancier.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin = userRepository.save(admin);

        System.out.println("✅ Utilisateur admin créé avec succès !");
        System.out.println("📝 Identifiants : username='admin', password='123456'");
        System.out.println("⚠️  IMPORTANT : Changez le mot de passe après la première connexion !");
    }
}

