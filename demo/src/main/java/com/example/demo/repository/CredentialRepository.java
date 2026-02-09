package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Credential;
import com.example.demo.entity.User;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    List<Credential> findByUser(User user);

    Optional<Credential> findByUserAndTypeAndActiveTrue(User user, String type);

    List<Credential> findByUserAndActiveTrue(User user);

    void deleteByUser(User user);
}
