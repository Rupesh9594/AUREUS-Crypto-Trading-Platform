package com.college.crypto.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class MarketService {

    private final RestTemplate restTemplate = new RestTemplate();

    // BTC price
    public double getBitcoinPrice() {

        String url =
                "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd";

        Map response = restTemplate.getForObject(url, Map.class);

        Map bitcoin = (Map) response.get("bitcoin");

        return Double.parseDouble(bitcoin.get("usd").toString());
    }

    // Top 50 market coins
    public List<Map<String, Object>> getTop50Cryptos() {

        String url =
                "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=50&page=1";

        List<Map<String, Object>> response =
                restTemplate.getForObject(url, List.class);

        return response;
    }
}