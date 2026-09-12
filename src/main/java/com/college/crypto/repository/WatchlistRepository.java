package com.college.crypto.repository;

import com.college.crypto.entity.Watchlist;
import com.college.crypto.entity.User;
import com.college.crypto.entity.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUser(User user);
    Optional<Watchlist> findByUserAndCrypto(User user, Crypto crypto);
}
