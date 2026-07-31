package com.cdl.ajustement.service;

import com.cdl.ajustement.entity.AppUser;
import com.cdl.ajustement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    public AppUser createUser(AppUser user) {
        // Encodage du mot de passe (on peut utiliser le matricule comme mdp par défaut
        // s'il n'est pas fourni)
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode("Cdl@2026")); // MDP par défaut sécurisé
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public AppUser updateUser(Long id, AppUser userDetails) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));

        user.setNom(userDetails.getNom());
        user.setPrenom(userDetails.getPrenom());
        user.setEmail(userDetails.getEmail());
        user.setMatricule(userDetails.getMatricule());
        user.setRole(userDetails.getRole());
        user.setAssignedDatabase(userDetails.getAssignedDatabase() != null ? userDetails.getAssignedDatabase() : "CDL_NEW");
        // Update allowed tabs if provided
        if (userDetails.getAllowedTabs() != null) {
            user.setAllowedTabs(userDetails.getAllowedTabs());
        }

        // On ne met à jour le mdp que s'il est fourni
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
