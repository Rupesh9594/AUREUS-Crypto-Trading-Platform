package com.college.crypto.repository;

import com.college.crypto.entity.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoRepository extends JpaRepository<Crypto, Long> {

    Optional<Crypto> findBySymbol(String symbol);
}