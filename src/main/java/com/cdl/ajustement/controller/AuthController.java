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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.domain}")
    private String ldapDomain;

    @Value("${ldap.domainComponent}")
    private String ldapDomainComponent;

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
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            // LDAP authentication
            com.cdl.ajustement.ldap.SessionLdap sessionLdap = com.cdl.ajustement.ldap.CommonLdap.connectSession(
                username, password, ldapUrl, ldapDomain, ldapDomainComponent);

            if (!sessionLdap.isLogged()) {
                return ResponseEntity.status(401).body("Invalid credentials or AD connection problem: " + sessionLdap.getLogError());
            }

            String employeeId = sessionLdap.getEmployeeID();
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return ResponseEntity.status(401).body("No employeeID found in AD for user");
            }

            AppUser appUser = userRepository.findByMatricule(employeeId).orElse(null);
            if (appUser == null) {
                return ResponseEntity.status(403).body("Access Denied: User matricule not listed in database.");
            }

            // Create Authentication object to simulate Spring Security auth
            String role = appUser.getRole() != null ? appUser.getRole().toUpperCase().trim() : "USER";
            User principal = new User(appUser.getUsername(), "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
            System.out.println("ROLE OF CONNECTED USER "+ role);
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String cleanRole = role.replace("ROLE_", "");

            // Get user's assigned database (which represents allowed databases)
            String allowedDatabases = (appUser.getAssignedDatabase() != null && !appUser.getAssignedDatabase().trim().isEmpty()) 
                                      ? appUser.getAssignedDatabase() : "CDL_NEW";

            // Déterminer la base de données selon le rôle et les autorisations
            String database;
            if ("BOTH".equals(allowedDatabases) || "ADMIN".equals(cleanRole) || "SUPER_ADMIN".equals(cleanRole)) {
                // L'utilisateur (ou l'admin) choisit sa base, avec fallback
                database = loginRequest.get("database");
                if (database == null || (!database.equals("CDL") && !database.equals("CDL_NEW"))) {
                    database = "CDL_NEW";
                }
            } else {
                // L'utilisateur est restreint à une seule base
                database = allowedDatabases;
            }

            // Tabs autorisés (fallback par défaut si null)
            String allowedTabs = appUser.getAllowedTabs();
            if (allowedTabs == null || allowedTabs.trim().isEmpty()) {
                allowedTabs = "home,dashboard,query-executor,monitoring";
            }

            // Re-positionner le contexte sur la bonne base pour le token
            DatabaseContextHolder.setDatabase(database);
            String jwt = jwtUtils.generateJwtToken(authentication);


            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", appUser.getUsername());
            response.put("role", cleanRole);
            response.put("database", database);
            response.put("allowedTabs", allowedTabs);
            response.put("allowedDatabases", ("ADMIN".equals(cleanRole) || "SUPER_ADMIN".equals(cleanRole)) ? "BOTH" : allowedDatabases);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        } finally {
            DatabaseContextHolder.clear();
        }
    }

    @PostMapping("/switch")
    public ResponseEntity<?> switchDatabase(@RequestParam String database, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Non autorisé");
        }

        String username = authentication.getName();
        AppUser appUser = userRepository.findByUsername(username).orElse(null);
        if (appUser == null) {
            return ResponseEntity.status(403).body("L'utilisateur n'existe pas dans le schéma " + database);
        }

        String assignedDb = appUser.getAssignedDatabase();
        String role = appUser.getRole() != null ? appUser.getRole().toUpperCase().trim() : "USER";
        String cleanRole = role.replace("ROLE_", "");

        boolean canSwitch = "BOTH".equals(assignedDb) || "ADMIN".equals(cleanRole) || "SUPER_ADMIN".equals(cleanRole);

        if (!canSwitch && !database.equals(assignedDb)) {
            return ResponseEntity.status(403).body("Vous n'êtes pas autorisé à accéder à cette base de données");
        }

        if (database == null || (!database.equals("CDL") && !database.equals("CDL_NEW"))) {
            return ResponseEntity.badRequest().body("Base de données invalide");
        }

        DatabaseContextHolder.setDatabase(database);
        try {
            String jwt = jwtUtils.generateJwtToken(authentication);

            String allowedTabs = appUser.getAllowedTabs();
            if (allowedTabs == null || allowedTabs.trim().isEmpty()) {
                allowedTabs = "home,dashboard,query-executor,monitoring";
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", username);
            response.put("role", cleanRole);
            response.put("database", database);
            response.put("allowedTabs", allowedTabs);
            response.put("allowedDatabases", ("ADMIN".equals(cleanRole) || "SUPER_ADMIN".equals(cleanRole)) ? "BOTH" : assignedDb);

            return ResponseEntity.ok(response);
        } finally {
            DatabaseContextHolder.clear();
        }
    }
}

