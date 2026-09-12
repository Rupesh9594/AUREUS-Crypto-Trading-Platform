package com.college.crypto.dto;

import java.math.BigDecimal;

public class TransactionResponseDTO {

    private String type;
    private String crypto;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;

    public TransactionResponseDTO(String type,
                                  String crypto,
                                  BigDecimal quantity,
                                  BigDecimal price,
                                  BigDecimal totalAmount) {

        this.type = type;
        this.crypto = crypto;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
    }

    public String getType() {
        return type;
    }

    public String getCrypto() {
        return crypto;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}