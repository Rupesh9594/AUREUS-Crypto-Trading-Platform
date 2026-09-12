package com.college.crypto.dto;

import java.math.BigDecimal;

public class WalletResponseDTO {

    private BigDecimal balance;

    public WalletResponseDTO() {}

    public WalletResponseDTO(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}