package com.college.crypto.config;

import com.college.crypto.entity.Crypto;
import com.college.crypto.repository.CryptoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CryptoRepository cryptoRepository;

    public DataInitializer(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public void run(String... args) {

        // Insert BTC if not exists
        if (cryptoRepository.findBySymbol("BTC").isEmpty()) {
            cryptoRepository.save(
                    new Crypto("Bitcoin", "BTC", new BigDecimal("60000"))
            );
        }

        // Insert ETH if not exists
        if (cryptoRepository.findBySymbol("ETH").isEmpty()) {
            cryptoRepository.save(
                    new Crypto("Ethereum", "ETH", new BigDecimal("3000"))
            );
        }

        // Insert SOL if not exists
        if (cryptoRepository.findBySymbol("SOL").isEmpty()) {
            cryptoRepository.save(
                    new Crypto("Solana", "SOL", new BigDecimal("150"))
            );
        }
    }
}