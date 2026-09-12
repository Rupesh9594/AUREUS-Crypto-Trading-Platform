package com.college.crypto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "user_holdings")
public class UserHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal quantity;

    // ⭐ NEW FIELD FOR PROFIT CALCULATION
    @Column(nullable = false)
    private BigDecimal averageBuyPrice;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "crypto_id", nullable = false)
    private Crypto crypto;

    public UserHolding() {}

    // ⭐ UPDATED CONSTRUCTOR
    public UserHolding(User user,
                       Crypto crypto,
                       BigDecimal quantity,
                       BigDecimal averageBuyPrice) {
        this.user = user;
        this.crypto = crypto;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(BigDecimal averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
    }

    public User getUser() {
        return user;
    }

    public Crypto getCrypto() {
        return crypto;
    }
}