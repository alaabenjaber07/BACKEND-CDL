package com.cdl.ajustement.entity;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "CDL_APP_USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // USER, ADMIN, SUPER_ADMIN

    private String nom;
    private String prenom;
    private String email;
    private String matricule;

    @Column(name = "ASSIGNED_DATABASE")
    private String assignedDatabase = "CDL_NEW"; // CDL ou CDL_NEW ou BOTH — assigné par l'admin

    @Column(name = "ALLOWED_TABS")
    private String allowedTabs; // e.g. "home,dashboard,query-executor,monitoring"
}
