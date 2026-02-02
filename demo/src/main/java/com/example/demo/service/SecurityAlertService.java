package com.example.demo.service;

import com.example.demo.entity.SecurityAlert;
import com.example.demo.repository.SecurityAlertRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityAlertService {
    
    @Autowired
    private SecurityAlertRepository securityAlertRepository;
    
    public SecurityAlert createAlert(String alertType, String username, String description, String severity) {
        SecurityAlert alert = new SecurityAlert(alertType, username, description, severity);
        return securityAlertRepository.save(alert);
    }
    
    public SecurityAlert createAlertWithIp(String alertType, String username, String description, 
                                           String severity, HttpServletRequest request) {
        SecurityAlert alert = new SecurityAlert(alertType, username, description, severity);
        alert.setIpAddress(getClientIP(request));
        return securityAlertRepository.save(alert);
    }
    
    public List<SecurityAlert> getAllAlerts() {
        return securityAlertRepository.findAll();
    }
    
    public List<SecurityAlert> getAlertsByUsername(String username) {
        return securityAlertRepository.findByUsername(username);
    }
    
    public List<SecurityAlert> getAlertsBySeverity(String severity) {
        return securityAlertRepository.findBySeverity(severity);
    }
    
    public List<SecurityAlert> getAlertsByType(String alertType) {
        return securityAlertRepository.findByAlertType(alertType);
    }
    
    public List<SecurityAlert> getAlertsInTimeRange(LocalDateTime start, LocalDateTime end) {
        return securityAlertRepository.findByCreatedAtBetween(start, end);
    }
    
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
