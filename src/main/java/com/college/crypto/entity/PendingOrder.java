package com.college.crypto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pending_orders")
public class PendingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "BUY" or "SELL"
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crypto_id")
    private Crypto crypto;

    private BigDecimal quantity;
    private BigDecimal targetPrice;

    public PendingOrder() {}

    public PendingOrder(String type, User user, Crypto crypto, BigDecimal quantity, BigDecimal targetPrice) {
        this.type = type;
        this.user = user;
        this.crypto = crypto;
        this.quantity = quantity;
        this.targetPrice = targetPrice;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public User getUser() { return user; }
    public Crypto getCrypto() { return crypto; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getTargetPrice() { return targetPrice; }

    public void setType(String type) { this.type = type; }
    public void setUser(User user) { this.user = user; }
    public void setCrypto(Crypto crypto) { this.crypto = crypto; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }
}
