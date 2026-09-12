package com.college.crypto.repository;

import com.college.crypto.entity.Wallet;
import com.college.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // 🔥 Find wallet by logged-in user
    Optional<Wallet> findByUser(User user);
}