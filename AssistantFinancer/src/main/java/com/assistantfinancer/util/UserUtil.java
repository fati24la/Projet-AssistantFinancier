package com.assistantfinancer.util;

import com.assistantfinancer.config.JwtService;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserUtil {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    public Long getUserIdFromAuth() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        System.out.println("🔐 [UserUtil] Authentication: " + (authentication != null ? "exists" : "null"));
        
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("❌ [UserUtil] Authentication is null or principal is null");
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        System.out.println("🔐 [UserUtil] Principal type: " + principal.getClass().getName());
        System.out.println("🔐 [UserUtil] Principal value: " + principal.toString());
        
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal.toString().equals("anonymousUser")) {
            System.out.println("❌ [UserUtil] User is anonymous");
            throw new RuntimeException("User not authenticated - anonymous user");
        } else {
            username = principal.toString();
        }

        System.out.println("🔐 [UserUtil] Extracted username: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ [UserUtil] User not found in database: " + username);
                    return new RuntimeException("User not found: " + username);
                });

        System.out.println("✅ [UserUtil] User found with ID: " + user.getId());
        return user.getId();
    }
}

