package com.example.demo.init;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Credential;
import com.example.demo.entity.Product;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.CredentialRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.HashUtil;

import jakarta.annotation.PostConstruct;

/**
 * Initialisation des données au démarrage (comme dans le cours).
 * Crée les rôles, utilisateurs, credentials et produits par défaut.
 */
@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Créer les rôles si absents
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER", "Standard user role")));
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Administrator role with full access")));
        Role moderatorRole = roleRepository.findByName("MODERATOR")
                .orElseGet(() -> roleRepository.save(new Role("MODERATOR", "Moderator role with limited admin access")));

        // Créer l'utilisateur admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@example.com", passwordEncoder.encode("adminpass"));
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            adminRoles.add(userRole);
            adminRoles.add(moderatorRole);
            admin.setRoles(adminRoles);
            admin = userRepository.save(admin);

            // Credential associé (séparation User/Credential comme dans le cours)
            Credential adminCred = new Credential(admin, "PASSWORD", HashUtil.hash("adminpass"));
            credentialRepository.save(adminCred);
        }

        // Créer un utilisateur standard (student1 comme Groupe 5)
        if (!userRepository.existsByUsername("student1")) {
            User student = new User("student1", "student1@example.com", passwordEncoder.encode("password"));
            Set<Role> studentRoles = new HashSet<>();
            studentRoles.add(userRole);
            student.setRoles(studentRoles);
            student = userRepository.save(student);

            Credential studentCred = new Credential(student, "PASSWORD", HashUtil.hash("password"));
            credentialRepository.save(studentCred);
        }

        // Créer un deuxième utilisateur standard
        if (!userRepository.existsByUsername("john_doe")) {
            User john = new User("john_doe", "john@example.com", passwordEncoder.encode("password123"));
            Set<Role> johnRoles = new HashSet<>();
            johnRoles.add(userRole);
            johnRoles.add(moderatorRole);
            john.setRoles(johnRoles);
            john = userRepository.save(john);

            Credential johnCred = new Credential(john, "PASSWORD", HashUtil.hash("password123"));
            credentialRepository.save(johnCred);
        }

        // Créer des produits par défaut (comme Groupe 5)
        if (productRepository.count() == 0) {
            productRepository.save(new Product("Honey"));
            productRepository.save(new Product("Almond"));
        }
    }
}
