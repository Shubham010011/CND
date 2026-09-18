package com.cryptox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WalletResponse {
    private String walletId;
    private List<WalletBalanceDto> balances;
}
