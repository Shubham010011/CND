package com.cryptox.controller;

import com.cryptox.dto.WalletResponse;
import com.cryptox.model.Wallet;
import com.cryptox.service.QrCodeService;
import com.cryptox.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final QrCodeService qrCodeService;

    /** Get the authenticated user's own wallet + balances. */
    @GetMapping("/me")
    public ResponseEntity<WalletResponse> myWallet(Authentication auth) {
        Wallet wallet = walletService.getWalletByOwnerUsername(auth.getName());
        return ResponseEntity.ok(walletService.toResponse(wallet));
    }

    /**
     * Look up any wallet by its public wallet ID (e.g. to confirm a
     * recipient's address before sending). Only exposes the walletId,
     * never balances or owner details, for privacy.
     */
    @GetMapping("/{walletId}")
    public ResponseEntity<Map<String, String>> lookup(@PathVariable String walletId) {
        Wallet wallet = walletService.getWalletByWalletId(walletId);
        return ResponseEntity.ok(Map.of("walletId", wallet.getWalletId(), "exists", "true"));
    }

    /**
     * QR code (base64 PNG) encoding the authenticated user's wallet ID —
     * same "receive" flow as MetaMask: another user scans it to auto-fill
     * the recipient address on their send screen.
     */
    @GetMapping("/me/qrcode")
    public ResponseEntity<Map<String, String>> myQrCode(Authentication auth) {
        Wallet wallet = walletService.getWalletByOwnerUsername(auth.getName());
        String base64Png = qrCodeService.generateQrCodeBase64(wallet.getWalletId(), 300);
        return ResponseEntity.ok(Map.of(
                "walletId", wallet.getWalletId(),
                "qrCodeBase64Png", base64Png
        ));
    }
}
