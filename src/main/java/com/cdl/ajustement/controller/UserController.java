package com.cdl.ajustement.controller;

import com.cdl.ajustement.entity.AppUser;
import com.cdl.ajustement.service.UserService;
import com.cdl.ajustement.config.DatabaseContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        String previousDb = DatabaseContextHolder.getDatabase();
        DatabaseContextHolder.setDatabase("CDL_NEW");
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } finally {
            DatabaseContextHolder.setDatabase(previousDb);
        }
    }

    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@RequestBody AppUser user) {
        String previousDb = DatabaseContextHolder.getDatabase();
        DatabaseContextHolder.setDatabase("CDL_NEW");
        try {
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest().body("Erreur: Le nom d'utilisateur est déjà utilisé !");
            }

            if (user.getMatricule() != null && !user.getMatricule().matches("\\d{4}")) {
                return ResponseEntity.badRequest().body("Erreur: Le matricule doit comporter exactement 4 chiffres !");
            }

            AppUser createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);
        } finally {
            DatabaseContextHolder.setDatabase(previousDb);
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody AppUser user) {
        String previousDb = DatabaseContextHolder.getDatabase();
        DatabaseContextHolder.setDatabase("CDL_NEW");
        try {
            AppUser updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        } finally {
            DatabaseContextHolder.setDatabase(previousDb);
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        String previousDb = DatabaseContextHolder.getDatabase();
        DatabaseContextHolder.setDatabase("CDL_NEW");
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        } finally {
            DatabaseContextHolder.setDatabase(previousDb);
        }
    }
}

