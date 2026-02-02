package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private SecurityAlertService securityAlertService;
    
    public String login(String username, String password, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            if (authentication.isAuthenticated()) {
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                
                // Créer alerte de succès
                securityAlertService.createAlertWithIp(
                    "LOGIN_SUCCESS", 
                    username, 
                    "User logged in successfully", 
                    "LOW",
                    request
                );
                
                return jwtUtil.generateToken(username);
            }
        } catch (BadCredentialsException e) {
            // Créer alerte d'échec
            securityAlertService.createAlertWithIp(
                "LOGIN_FAILED", 
                username, 
                "Failed login attempt - Invalid credentials", 
                "MEDIUM",
                request
            );
            throw new RuntimeException("Invalid username or password");
        }
        
        throw new RuntimeException("Authentication failed");
    }
    
    public User register(String username, String email, String password, HttpServletRequest request) {
        if (userRepository.existsByUsername(username)) {
            securityAlertService.createAlertWithIp(
                "REGISTRATION_FAILED", 
                username, 
                "Attempted registration with existing username", 
                "LOW",
                request
            );
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            securityAlertService.createAlertWithIp(
                "REGISTRATION_FAILED", 
                email, 
                "Attempted registration with existing email", 
                "LOW",
                request
            );
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User(username, email, passwordEncoder.encode(password));
        
        // Assigner le rôle USER par défaut
        Optional<Role> userRole = roleRepository.findByName("USER");
        if (userRole.isPresent()) {
            Set<Role> roles = new HashSet<>();
            roles.add(userRole.get());
            user.setRoles(roles);
        }
        
        User savedUser = userRepository.save(user);
        
        securityAlertService.createAlertWithIp(
            "REGISTRATION_SUCCESS", 
            username, 
            "New user registered successfully", 
            "LOW",
            request
        );
        
        return savedUser;
    }
    
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
