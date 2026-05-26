package com.cdl.ajustement.controller;

import com.cdl.ajustement.service.QueryExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/queries")
@CrossOrigin(origins = "*")
public class QueryExecutionController {

    @Autowired
    private QueryExecutionService executionService;

    @Autowired
    private com.cdl.ajustement.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private boolean verifyUserPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) return false;
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        com.cdl.ajustement.entity.AppUser user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            return passwordEncoder.matches(rawPassword, user.getPassword());
        }
        return false;
    }

    @PostMapping("/reset-default")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> resetDefaultConfig() {
        executionService.updateDefaultConfig();
        return ResponseEntity.ok(Collections.singletonMap("status", "Default configuration reset"));
    }

    @GetMapping("/configs")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'USER')")
    public java.util.List<com.cdl.ajustement.entity.QueryConfig> getAllConfigs() {
        return executionService.getAllConfigs();
    }

    @PostMapping("/configs")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public com.cdl.ajustement.entity.QueryConfig createConfig(
            @RequestBody com.cdl.ajustement.entity.QueryConfig config) {
        return executionService.saveConfig(config);
    }

    @PutMapping("/configs/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public com.cdl.ajustement.entity.QueryConfig updateConfig(@PathVariable Long id,
            @RequestBody com.cdl.ajustement.entity.QueryConfig config) {
        config.setId(id);
        return executionService.saveConfig(config);
    }

    @DeleteMapping("/configs/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        executionService.deleteConfig(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logs")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public java.util.List<com.cdl.ajustement.entity.QueryExecutionLog> getExecutionLogs() {
        return executionService.getAllExecutionLogs();
    }

    @GetMapping("/extraction-logs")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public java.util.List<com.cdl.ajustement.entity.QueryExtractionLog> getExtractionLogs() {
        return executionService.getAllExtractionLogs();
    }

    @PostMapping("/execute/{configName}")
    public ResponseEntity<Map<String, String>> executeQuery(@PathVariable String configName, @RequestBody Map<String, String> body) {
        if (!verifyUserPassword(body.get("password"))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Mot de passe invalide"));
        }
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        executionService.executeConfiguredQuery(configName, username);
        return ResponseEntity.ok(Collections.singletonMap("status", "Execution started"));
    }

    @PostMapping("/schedule/{configName}")
    public ResponseEntity<Map<String, String>> scheduleQuery(@PathVariable String configName, @RequestBody Map<String, String> body) {
        if (!verifyUserPassword(body.get("password"))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Mot de passe invalide"));
        }
        
        // Prevent scheduling if already running or scheduled
        com.cdl.ajustement.entity.ScheduledTask existing = executionService.getPendingSchedule(configName);
        if (existing != null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Une exécution est déjà planifiée pour ce traitement."));
        }

        String scheduleTimeStr = body.get("scheduleTime");
        if (scheduleTimeStr == null || scheduleTimeStr.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Date de planification manquante"));
        }
        java.time.LocalDateTime scheduleTime = java.time.LocalDateTime.parse(scheduleTimeStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        
        executionService.scheduleExecution(configName, scheduleTime, username);
        return ResponseEntity.ok(Collections.singletonMap("status", "Execution scheduled"));
    }

    @GetMapping("/schedule-status/{configName}")
    public ResponseEntity<Map<String, Object>> getScheduleStatus(@PathVariable String configName) {
        com.cdl.ajustement.entity.ScheduledTask existing = executionService.getPendingSchedule(configName);
        Map<String, Object> response = new java.util.HashMap<>();
        if (existing != null) {
            response.put("isScheduled", true);
            response.put("scheduledTime", existing.getScheduledTime().toString());
            response.put("scheduledBy", existing.getScheduledBy());
        } else {
            response.put("isScheduled", false);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel/{configName}")
    public ResponseEntity<Map<String, String>> cancelQuery(@PathVariable String configName) {
        executionService.cancelExecution(configName);
        return ResponseEntity.ok(Collections.singletonMap("status", "Execution cancelled"));
    }

    @PostMapping("/cancel-secure/{configName}")
    public ResponseEntity<Map<String, String>> cancelQuerySecure(@PathVariable String configName,
            @RequestBody Map<String, String> body) {
        if (!verifyUserPassword(body.get("password"))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Mot de passe invalide"));
        }
        executionService.cancelExecution(configName);
        return ResponseEntity.ok(Collections.singletonMap("status", "Execution cancelled"));
    }

    @PostMapping("/cancel-schedule/{configName}")
    public ResponseEntity<Map<String, String>> cancelSchedule(@PathVariable String configName, @RequestBody Map<String, String> body) {
        if (!verifyUserPassword(body.get("password"))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Mot de passe invalide"));
        }
        executionService.cancelScheduledExecution(configName);
        return ResponseEntity.ok(Collections.singletonMap("status", "Schedule cancelled"));
    }

    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getProgress(
            @RequestParam(defaultValue = "default_process") String configName) {
        return ResponseEntity.ok(executionService.getExecutionProgress(configName));
    }

    @PostMapping("/cancel-extraction/{configName}/{index}")
    public ResponseEntity<Map<String, String>> cancelExtraction(
            @PathVariable String configName,
            @PathVariable int index) {
        executionService.cancelExtraction(configName, index);
        return ResponseEntity.ok(Collections.singletonMap("status", "Extraction cancelled"));
    }

    @PostMapping("/start-extraction/{configName}/{index}")
    public ResponseEntity<Void> startExtraction(
            @PathVariable String configName,
            @PathVariable int index) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        executionService.startExtractionBackground(configName, index, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/extraction-status/{configName}/{index}")
    public ResponseEntity<com.cdl.ajustement.entity.QueryExtractionLog> getExtractionStatus(
            @PathVariable String configName,
            @PathVariable int index) {
        return ResponseEntity.ok(executionService.getExtractionStatus(configName, index));
    }

    @GetMapping("/download-extraction/{configName}/{index}")
    public void downloadExtraction(
            @PathVariable String configName,
            @PathVariable int index,
            javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        com.cdl.ajustement.entity.QueryExtractionLog log = executionService.getExtractionStatus(configName, index);
        sendFileResponse(log, configName + "_ext_" + index + ".csv", response);
    }

    @GetMapping("/download-log/{id}")
    public void downloadLogById(
            @PathVariable Long id,
            javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        com.cdl.ajustement.entity.QueryExtractionLog log = executionService.getExtractionLogById(id);
        if (log == null) {
            response.sendError(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND, "Log non trouvé");
            return;
        }
        String filename = log.getConfigName() + "_ext_" + log.getExtractionIndex() + ".csv";
        sendFileResponse(log, filename, response);
    }

    private void sendFileResponse(com.cdl.ajustement.entity.QueryExtractionLog log, String filename,
            javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        if (log == null) {
            response.sendError(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND, "Aucun log d'extraction trouvé.");
            return;
        }
        if (!"SUCCESS".equals(log.getStatus())) {
            response.sendError(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST,
                    "Statut invalide: " + log.getStatus());
            return;
        }
        if (log.getFilePath() == null) {
            response.sendError(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Fichier manquant dans le log.");
            return;
        }

        java.io.File file = new java.io.File(log.getFilePath());
        if (!file.exists()) {
            response.sendError(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND, "Fichier physique introuvable.");
            return;
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "must-revalidate");
        response.setContentLength((int) file.length());

        try (java.io.InputStream in = new java.io.FileInputStream(file);
                java.io.OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
