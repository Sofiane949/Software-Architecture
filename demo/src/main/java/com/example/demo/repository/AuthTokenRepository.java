package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.AuthToken;
import com.example.demo.entity.User;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByValue(String value);

    List<AuthToken> findByUser(User user);

    List<AuthToken> findByUserAndRevokedFalse(User user);

    void deleteByUser(User user);
}
