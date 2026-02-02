package com.example.demo.controller;

import com.example.demo.entity.SecurityAlert;
import com.example.demo.service.SecurityAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/security-alerts")
public class SecurityAlertController {
    
    @Autowired
    private SecurityAlertService securityAlertService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAlert>> getAllAlerts() {
        return ResponseEntity.ok(securityAlertService.getAllAlerts());
    }
    
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<List<SecurityAlert>> getAlertsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(securityAlertService.getAlertsByUsername(username));
    }
    
    @GetMapping("/severity/{severity}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAlert>> getAlertsBySeverity(@PathVariable String severity) {
        return ResponseEntity.ok(securityAlertService.getAlertsBySeverity(severity));
    }
    
    @GetMapping("/type/{alertType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAlert>> getAlertsByType(@PathVariable String alertType) {
        return ResponseEntity.ok(securityAlertService.getAlertsByType(alertType));
    }
}
