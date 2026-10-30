package com.cryptox.repository;

import com.cryptox.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String v);

    Optional<User> findByEmail(String v);

    boolean existsByUsername(String v);

    boolean existsByEmail(String v);
}
