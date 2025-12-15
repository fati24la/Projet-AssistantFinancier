package com.assistantfinancer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;
    
    @Autowired(required = false)
    private CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                    if (corsConfigurationSource != null) {
                        cors.configurationSource(corsConfigurationSource);
                    }
                })
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()   // login + register
                        .requestMatchers("/api/admin/**").authenticated()  // Admin endpoints nécessitent authentification
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()  // Autoriser OPTIONS pour CORS preflight
                        .anyRequest().authenticated()                 // le reste nécessite JWT
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // FILTRE JWT
    @Bean
    public OncePerRequestFilter jwtAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {

                String authHeader = request.getHeader("Authorization");
                String method = request.getMethod();
                String uri = request.getRequestURI();
                
                System.out.println("🔍 [JWT Filter] Requête: " + method + " " + uri);
                System.out.println("🔍 [JWT Filter] Authorization header: " + (authHeader != null ? "présent" : "absent"));
                
                // Autoriser les requêtes OPTIONS (preflight CORS) sans authentification
                if ("OPTIONS".equalsIgnoreCase(method)) {
                    System.out.println("✅ [JWT Filter] Requête OPTIONS (preflight), autorisation automatique");
                    filterChain.doFilter(request, response);
                    return;
                }

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    try {
                        System.out.println("🔐 [JWT Filter] Tentative de validation du token...");
                        String username = jwtService.extractUsername(token);
                        System.out.println("✅ [JWT Filter] Token valide pour l'utilisateur: " + username);

                        if (username != null) {
                            // Toujours définir l'authentification, même si elle existe déjà
                            UserDetails user = org.springframework.security.core.userdetails.User
                                    .withUsername(username)
                                    .password("")   // pas utilisé
                                    .authorities("USER", "ADMIN") // Ajouter les deux rôles
                                    .build();

                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            user, null, user.getAuthorities());

                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            System.out.println("✅ [JWT Filter] Authentification définie dans SecurityContext pour: " + username);
                            System.out.println("✅ [JWT Filter] Autorités: " + user.getAuthorities());
                        }
                    } catch (Exception e) {
                        // Token invalide ou expiré
                        System.out.println("❌ [JWT Filter] Erreur de validation du token: " + e.getMessage());
                        e.printStackTrace();
                        // On continue pour que Spring Security gère avec 401/403
                    }
                } else {
                    System.out.println("⚠️ [JWT Filter] Pas de header Authorization ou format incorrect");
                    // Pour les requêtes DELETE/PUT, si pas de token, on laisse Spring Security gérer (retournera 401/403)
                    if (uri.startsWith("/api/admin/") && ("DELETE".equals(method) || "PUT".equals(method) || "POST".equals(method))) {
                        System.out.println("❌ [JWT Filter] Requête " + method + " vers " + uri + " sans token - sera rejetée avec 403");
                    }
                }
                
                // Vérifier l'état de l'authentification après traitement
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    System.out.println("✅ [JWT Filter] Authentification active: " + SecurityContextHolder.getContext().getAuthentication().getName());
                    System.out.println("✅ [JWT Filter] Autorités: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                } else {
                    System.out.println("⚠️ [JWT Filter] Aucune authentification dans SecurityContext");
                    if (uri.startsWith("/api/admin/") && ("DELETE".equals(method) || "PUT".equals(method) || "POST".equals(method))) {
                        System.out.println("❌ [JWT Filter] Requête " + method + " vers " + uri + " sans authentification - sera rejetée avec 403");
                    }
                }

                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
