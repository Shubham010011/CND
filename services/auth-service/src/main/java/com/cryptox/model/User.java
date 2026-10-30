package com.cryptox.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "app_user", uniqueConstraints = { @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String username;
    @Column(nullable = false, length = 120)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private boolean enabled;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private String walletId;

    @PrePersist
    void pre() {
        createdAt = Instant.now();
    }
}
