package com.college.crypto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal balance;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Wallet() {}

    public Wallet(BigDecimal balance, User user) {
        this.balance = balance;
        this.user = user;
    }

    public Long getId() { return id; }

    public BigDecimal getBalance() { return balance; }

    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}