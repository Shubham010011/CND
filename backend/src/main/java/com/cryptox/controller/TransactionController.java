package com.cryptox.controller;

import com.cryptox.dto.SendCryptoRequest;
import com.cryptox.dto.TransactionResponse;
import com.cryptox.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /** Send crypto to another wallet, identified by wallet ID (typed manually or captured via QR scan). */
    @PostMapping("/send")
    public ResponseEntity<TransactionResponse> send(Authentication auth, @Valid @RequestBody SendCryptoRequest request) {
        return ResponseEntity.ok(transactionService.sendCrypto(auth.getName(), request));
    }

    /** Full send + receive history for the authenticated user's wallet. */
    @GetMapping("/history")
    public ResponseEntity<List<TransactionResponse>> history(Authentication auth) {
        return ResponseEntity.ok(transactionService.getHistoryForUser(auth.getName()));
    }
}
