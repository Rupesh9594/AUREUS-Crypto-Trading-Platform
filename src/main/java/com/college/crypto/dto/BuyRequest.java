package com.college.crypto.dto;

import java.math.BigDecimal;

public class BuyRequest {

    private String symbol;
    private BigDecimal quantity;

    public BuyRequest() {}

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}