package com.college.crypto.controller;

import com.college.crypto.payload.ApiResponse;
import com.college.crypto.service.MarketService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    // BTC Price
    @GetMapping("/btc")
    public ApiResponse<?> getBitcoinPrice() {

        double price = marketService.getBitcoinPrice();

        return new ApiResponse<>(
                true,
                "Bitcoin price fetched successfully",
                price
        );
    }

    // TOP 50 CRYPTOS
    @GetMapping("/top50")
    public ApiResponse<?> getTop50Cryptos() {

        return new ApiResponse<>(
                true,
                "Top 50 cryptocurrencies fetched",
                marketService.getTop50Cryptos()
        );
    }


}