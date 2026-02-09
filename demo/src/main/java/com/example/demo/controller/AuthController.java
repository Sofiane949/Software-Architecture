package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;
    
    // POST /api/auth/login — authentification, renvoie un token
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, 
                                   HttpServletRequest request) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            String token = authService.login(username, password, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("username", username);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // POST /api/auth/register — inscription d'un nouvel utilisateur
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest,
                                     HttpServletRequest request) {
        try {
            String username = registerRequest.get("username");
            String email = registerRequest.get("email");
            String password = registerRequest.get("password");
            
            authService.register(username, email, password, request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("username", username);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // POST /api/auth/validate — validation de token (par body JSON)
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            String token = tokenRequest.get("token");
            boolean isValid = authService.validateToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // GET /api/auth/validate/{token} — validation de token par URL (comme Groupe 5)
    @GetMapping("/validate/{token}")
    public ResponseEntity<Object> validateTokenByPath(@PathVariable String token) {
        boolean isValid = authService.validateToken(token);
        if (isValid) {
            return ResponseEntity.ok().body("Token valid");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalid or expired");
    }

    // DELETE /api/auth/logout/{token} — révocation du token (logout)
    @DeleteMapping("/logout/{token}")
    public ResponseEntity<Object> logout(@PathVariable String token) {
        authService.revokeToken(token);
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    // GET /api/auth/users — lister tous les utilisateurs (debug/admin)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }
}
