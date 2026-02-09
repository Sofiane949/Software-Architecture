package com.example.demo.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Credential;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.AuthTokenRepository;
import com.example.demo.repository.CredentialRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.HashUtil;
import com.example.demo.util.JwtUtil;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // POST /api/admin/users — créer un utilisateur (body: {"username":"name", "email":"email"})
    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");

        if (username == null || username.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("username required");
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("user already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email != null ? email : username + "@example.com");
        // mot de passe temporaire
        user.setPassword(passwordEncoder.encode("changeme"));

        // Assigner rôle USER par défaut
        Optional<Role> userRole = roleRepository.findByName("USER");
        if (userRole.isPresent()) {
            Set<Role> roles = new HashSet<>();
            roles.add(userRole.get());
            user.setRoles(roles);
        }

        User saved = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("username", saved.getUsername());
        response.put("id", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/admin/users/{username}/credentials — ajouter un credential
    @PostMapping("/users/{username}/credentials")
    public ResponseEntity<Object> addCredential(@PathVariable String username,
                                                @RequestBody Map<String, String> body) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found");
        }

        String type = body.getOrDefault("type", "PASSWORD");
        String secret = body.get("secret");
        if (secret == null || secret.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("secret required");
        }

        User user = optUser.get();

        Credential cred = new Credential();
        cred.setUser(user);
        cred.setType(type);
        cred.setSecretHash(HashUtil.hash(secret));
        cred.setActive(true);

        // Si c'est un PASSWORD, mettre aussi à jour le password du User (pour Spring Security)
        if ("PASSWORD".equals(type)) {
            user.setPassword(passwordEncoder.encode(secret));
            userRepository.save(user);
        }

        credentialRepository.save(cred);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Credential added successfully");
        response.put("type", type);
        response.put("username", username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/admin/users/{username}/credentials — lister les credentials d'un user
    @GetMapping("/users/{username}/credentials")
    public ResponseEntity<Object> getCredentials(@PathVariable String username) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found");
        }

        List<Credential> credentials = credentialRepository.findByUser(optUser.get());
        List<Map<String, Object>> result = credentials.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("type", c.getType());
            map.put("active", c.isActive());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // DELETE /api/admin/users/{username} — supprimer un utilisateur et ses données associées
    @DeleteMapping("/users/{username}")
    public ResponseEntity<Object> deleteUser(@PathVariable String username) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found");
        }

        User user = optUser.get();

        // Supprimer les tokens associés
        authTokenRepository.deleteByUser(user);

        // Supprimer les credentials associés
        credentialRepository.deleteByUser(user);

        // Supprimer l'utilisateur
        userRepository.delete(user);

        return ResponseEntity.ok("User deleted successfully");
    }
}
