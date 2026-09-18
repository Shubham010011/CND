package com.cryptox.service;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generates a unique, MetaMask-style wallet address:
 * SHA-256 hash of a random UUID + random salt, hex-encoded and
 * prefixed with "0x". This is a deterministic-looking but
 * non-reversible public identifier for the internal ledger wallet.
 */
@Component
public class WalletAddressGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);

            String seed = UUID.randomUUID().toString() + System.nanoTime() + bytesToHex(salt);
            byte[] hash = digest.digest(seed.getBytes());

            // Take first 20 bytes (40 hex chars) like an Ethereum address, prefix with 0x
            String hex = bytesToHex(hash).substring(0, 40);
            return "0x" + hex;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
