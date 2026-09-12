package com.college.crypto.dto;

import java.math.BigDecimal;

public class ProfitResponseDTO {

    private String symbol;
    private BigDecimal quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal currentPrice;
    private BigDecimal profit;

    public ProfitResponseDTO(String symbol,
                             BigDecimal quantity,
                             BigDecimal avgBuyPrice,
                             BigDecimal currentPrice,
                             BigDecimal profit) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
        this.currentPrice = currentPrice;
        this.profit = profit;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getProfit() {
        return profit;
    }
}