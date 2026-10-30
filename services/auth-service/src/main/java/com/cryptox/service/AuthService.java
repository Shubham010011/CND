package com.cryptox.service;

import com.cryptox.dto.*;
import com.cryptox.model.User;
import com.cryptox.repository.UserRepository;
import com.cryptox.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AuthService {
    final UserRepository repo;
    final PasswordEncoder encoder;
    final JwtUtil jwt;
    final RestClient.Builder rest;
    @Value("${services.wallet.url}")
    String walletUrl;
    @Value("${app.internal-key}")
    String internalKey;

    public AuthResponse register(RegisterRequest r) {
        if (repo.existsByUsername(r.getUsername()))
            throw new IllegalArgumentException("Username already taken");
        if (repo.existsByEmail(r.getEmail()))
            throw new IllegalArgumentException("Email already registered");
        var u = repo.save(User.builder().username(r.getUsername()).email(r.getEmail())
                .passwordHash(encoder.encode(r.getPassword())).enabled(true).build());
        try {
            var w = rest.build().post().uri(walletUrl + "/internal/wallets").header("X-Internal-Key", internalKey)
                    .body(java.util.Map.of("username", u.getUsername())).retrieve().body(WalletCreateResponse.class);
            u.setWalletId(w.walletId());
            repo.save(u);
            return new AuthResponse(jwt.generate(u.getUsername()), "Bearer", u.getUsername(), u.getWalletId());
        } catch (Exception e) {
            repo.delete(u);
            throw new IllegalStateException("Wallet service unavailable during registration");
        }
    }

    public AuthResponse login(LoginRequest r) {
        var u = repo.findByUsername(r.getUsernameOrEmail()).or(() -> repo.findByEmail(r.getUsernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username/email or password"));
        if (!encoder.matches(r.getPassword(), u.getPasswordHash()))
            throw new IllegalArgumentException("Invalid username/email or password");
        return new AuthResponse(jwt.generate(u.getUsername()), "Bearer", u.getUsername(), u.getWalletId());
    }
}