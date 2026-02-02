package com.example.demo.repository;

import com.example.demo.entity.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {
    
    List<SecurityAlert> findByUsername(String username);
    
    List<SecurityAlert> findBySeverity(String severity);
    
    List<SecurityAlert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<SecurityAlert> findByAlertType(String alertType);
}
