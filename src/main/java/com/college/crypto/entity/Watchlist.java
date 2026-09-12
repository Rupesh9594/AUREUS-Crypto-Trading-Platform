package com.college.crypto.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "watchlist")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crypto_id")
    private Crypto crypto;

    public Watchlist() {}
    public Watchlist(User user, Crypto crypto) {
        this.user = user;
        this.crypto = crypto;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Crypto getCrypto() { return crypto; }

    public void setUser(User user) { this.user = user; }
    public void setCrypto(Crypto crypto) { this.crypto = crypto; }
}
