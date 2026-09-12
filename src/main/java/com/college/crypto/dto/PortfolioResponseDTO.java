package com.college.crypto.dto;

import java.math.BigDecimal;

public class PortfolioResponseDTO {

    private String symbol;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;

    public PortfolioResponseDTO() {
    }

    public PortfolioResponseDTO(String symbol,
                                BigDecimal quantity,
                                BigDecimal currentPrice,
                                BigDecimal totalValue) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.currentPrice = currentPrice;
        this.totalValue = totalValue;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }
}