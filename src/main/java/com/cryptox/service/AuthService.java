package com.cryptox.service;

import com.cryptox.dto.AuthResponse;
import com.cryptox.dto.LoginRequest;
import com.cryptox.dto.RegisterRequest;
import com.cryptox.exception.ApiException;
import com.cryptox.model.Role;
import com.cryptox.model.User;
import com.cryptox.model.Wallet;
import com.cryptox.repository.UserRepository;
import com.cryptox.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final WalletService walletService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException("Username already taken", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                // Password hashing: BCrypt with per-password salt, never store plaintext.
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .build();

        user = userRepository.save(user);

        // Every customer automatically gets a wallet, MetaMask-style, at signup.
        Wallet wallet = walletService.createWalletForUser(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), wallet.getWalletId());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        String token = jwtUtil.generateToken(user.getUsername());
        String walletId = user.getWallet() != null ? user.getWallet().getWalletId() : null;
        return new AuthResponse(token, user.getUsername(), walletId);
    }
}
