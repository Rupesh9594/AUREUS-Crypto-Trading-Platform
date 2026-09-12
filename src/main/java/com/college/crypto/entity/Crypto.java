package com.college.crypto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cryptos")
public class Crypto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private BigDecimal currentPrice;

    public Crypto() {}

    public Crypto(String name, String symbol, BigDecimal currentPrice) {
        this.name = name;
        this.symbol = symbol;
        this.currentPrice = currentPrice;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}