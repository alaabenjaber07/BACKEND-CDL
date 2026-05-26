package com.cdl.ajustement.config;

import com.cdl.ajustement.entity.AppUser;
import com.cdl.ajustement.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class DataInitializer {
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(JdbcTemplate jdbcTemplate, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initManagedTables() {
        // Initialisation de CDL_NEW
        DatabaseContextHolder.setDatabase("CDL_NEW");
        initializeForCurrentContext();

        // Initialisation de CDL
        DatabaseContextHolder.setDatabase("CDL");
        initializeForCurrentContext();

        DatabaseContextHolder.clear();
    }

    private void initializeForCurrentContext() {
        try {
            jdbcTemplate.execute("CREATE TABLE CDL_MANAGED_TABLES (TABLE_NAME VARCHAR2(100) PRIMARY KEY)");
        } catch (Exception e) {
            // Table existe déjà
        }
        
        String[] defaultTables = {"CDL_AJUSTEMENT", "CDL_FNE", "CDL_FNG","ajust_eps_contre_gar","ajust_cde",
                "cdl_nantissement","CDL_DEPOT", "ajust_ctx","ajust_prosol","cdl_gar_bque","cdl_gar_etat",
                "cdl_sotugar","cdl_gar_hyp"};
        for (String tbl : defaultTables) {
            try {
                jdbcTemplate.update("INSERT INTO CDL_MANAGED_TABLES (TABLE_NAME) VALUES (?)", tbl);
            } catch (Exception e) {}
        }

        // S'assurer que les colonnes nécessaires existent dans la table CDL_APP_USER
        try {
            jdbcTemplate.execute("ALTER TABLE CDL_APP_USER ADD NOM VARCHAR2(100)");
        } catch (Exception e) {}
        try {
            jdbcTemplate.execute("ALTER TABLE CDL_APP_USER ADD PRENOM VARCHAR2(100)");
        } catch (Exception e) {}
        try {
            jdbcTemplate.execute("ALTER TABLE CDL_APP_USER ADD EMAIL VARCHAR2(100)");
        } catch (Exception e) {}
        try {
            jdbcTemplate.execute("ALTER TABLE CDL_APP_USER ADD MATRICULE VARCHAR2(100)");
        } catch (Exception e) {}
        try {
            jdbcTemplate.execute("ALTER TABLE CDL_APP_USER ADD ASSIGNED_DATABASE VARCHAR2(50) DEFAULT 'CDL_NEW'");
        } catch (Exception e) {}

        if (!userRepository.findByUsername("admin").isPresent()) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }

        if (!userRepository.findByUsername("user").isPresent()) {
            AppUser user = new AppUser();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user"));
            user.setRole("USER");
            userRepository.save(user);
        }
    }
}
