package com.college.crypto.controller;

import com.college.crypto.entity.Crypto;
import com.college.crypto.entity.User;
import com.college.crypto.entity.Watchlist;
import com.college.crypto.payload.ApiResponse;
import com.college.crypto.repository.CryptoRepository;
import com.college.crypto.repository.UserRepository;
import com.college.crypto.repository.WatchlistRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final CryptoRepository cryptoRepository;

    public WatchlistController(WatchlistRepository watchlistRepository, UserRepository userRepository, CryptoRepository cryptoRepository) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.cryptoRepository = cryptoRepository;
    }

    @GetMapping
    public ApiResponse<?> getWatchlist() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        List<Map<String, Object>> res = watchlistRepository.findByUser(user).stream().map(w -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", w.getId());
            map.put("symbol", w.getCrypto().getSymbol());
            map.put("name", w.getCrypto().getName());
            map.put("currentPrice", w.getCrypto().getCurrentPrice());
            return map;
        }).collect(Collectors.toList());

        return new ApiResponse<>(true, "Watchlist fetched", res);
    }

    @PostMapping("/add")
    public ApiResponse<?> addToWatchlist(@RequestBody Map<String, String> body) {
        String symbol = body.get("symbol");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Crypto crypto = cryptoRepository.findBySymbol(symbol.toUpperCase()).orElseThrow();

        if (watchlistRepository.findByUserAndCrypto(user, crypto).isEmpty()) {
            watchlistRepository.save(new Watchlist(user, crypto));
            return new ApiResponse<>(true, "Added to watchlist", null);
        }
        return new ApiResponse<>(false, "Already in watchlist", null);
    }

    @PostMapping("/remove")
    public ApiResponse<?> removeFromWatchlist(@RequestBody Map<String, String> body) {
        String symbol = body.get("symbol");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Crypto crypto = cryptoRepository.findBySymbol(symbol.toUpperCase()).orElseThrow();

        watchlistRepository.findByUserAndCrypto(user, crypto).ifPresent(watchlistRepository::delete);
        return new ApiResponse<>(true, "Removed from watchlist", null);
    }
}
