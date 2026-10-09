package com.example.LoginCompleto.auth.repository;

import com.example.LoginCompleto.auth.entity.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface IPasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    boolean existsByUserIdAndCreatedAtAfter(String userId, Instant createdAt);

    long deleteByTokenHash(String tokenHash);

    void deleteByUserId(String userId);
}
