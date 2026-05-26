package com.cdl.ajustement.controller;

import com.cdl.ajustement.entity.AppUser;
import com.cdl.ajustement.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import com.cdl.ajustement.config.DatabaseContextHolder;
import com.cdl.ajustement.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;


    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        // On commence par CDL_NEW pour l'authentification initiale
        DatabaseContextHolder.setDatabase("CDL_NEW");
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password")));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User principal = (User) authentication.getPrincipal();
            String role = principal.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse("");
            String cleanRole = role.replace("ROLE_", "");

            // Déterminer la base de données selon le rôle
            String database;
            if ("ADMIN".equals(cleanRole) || "SUPER_ADMIN".equals(cleanRole)) {
                // L'admin/super_admin choisit sa base
                database = loginRequest.get("database");
                if (database == null || (!database.equals("CDL") && !database.equals("CDL_NEW"))) {
                    database = "CDL_NEW";
                }
            } else {
                // L'utilisateur normal utilise la base assignée par l'admin
                AppUser appUser = userRepository.findByUsername(principal.getUsername()).orElse(null);
                database = (appUser != null && appUser.getAssignedDatabase() != null) 
                           ? appUser.getAssignedDatabase() : "CDL_NEW";
            }

            // Re-positionner le contexte sur la bonne base pour le token
            DatabaseContextHolder.setDatabase(database);
            String jwt = jwtUtils.generateJwtToken(authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", principal.getUsername());
            response.put("role", cleanRole);
            response.put("database", database);

            return ResponseEntity.ok(response);
        } finally {
            DatabaseContextHolder.clear();
        }
    }

    @PostMapping("/switch")
    public ResponseEntity<?> switchDatabase(@RequestParam String database, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Non autorisé");
        }

        // Seuls ADMIN et SUPER_ADMIN peuvent switcher dynamiquement
        String role = authentication.getAuthorities().stream()
                .findFirst().map(GrantedAuthority::getAuthority).orElse("");
        String cleanRole = role.replace("ROLE_", "");
        if (!"ADMIN".equals(cleanRole) && !"SUPER_ADMIN".equals(cleanRole)) {
            return ResponseEntity.status(403).body("Seuls les administrateurs peuvent changer de base de données");
        }

        if (database == null || (!database.equals("CDL") && !database.equals("CDL_NEW"))) {
            return ResponseEntity.badRequest().body("Base de données invalide");
        }

        DatabaseContextHolder.setDatabase(database);
        try {
            String username = authentication.getName();
            if (!userRepository.findByUsername(username).isPresent()) {
                return ResponseEntity.status(403).body("L'utilisateur n'existe pas dans le schéma " + database);
            }

            String jwt = jwtUtils.generateJwtToken(authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", username);
            response.put("role", cleanRole);
            response.put("database", database);

            return ResponseEntity.ok(response);
        } finally {
            DatabaseContextHolder.clear();
        }
    }
}

