package com.college.crypto.scheduler;

import com.college.crypto.entity.Crypto;
import com.college.crypto.entity.User;
import com.college.crypto.repository.CryptoRepository;
import com.college.crypto.repository.UserRepository;
import com.college.crypto.service.DashboardService;
import com.college.crypto.service.MarketService;
import com.college.crypto.dto.DashboardResponseDTO;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CryptoPriceScheduler {

    private final MarketService marketService;
    private final CryptoRepository cryptoRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final DashboardService dashboardService;

    public CryptoPriceScheduler(MarketService marketService,
                                CryptoRepository cryptoRepository,
                                SimpMessagingTemplate messagingTemplate,
                                UserRepository userRepository,
                                DashboardService dashboardService) {
        this.marketService = marketService;
        this.cryptoRepository = cryptoRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
    }

    // Runs every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void updateCryptoPrices() {

        List<Map<String, Object>> coins = marketService.getTop50Cryptos();

        for (Map<String, Object> coin : coins) {

            String symbol = coin.get("symbol").toString().toUpperCase();
            String name = coin.get("name").toString();
            Double price = Double.parseDouble(coin.get("current_price").toString());

            Crypto crypto = cryptoRepository.findBySymbol(symbol)
                    .orElseGet(() -> {
                        Crypto newCrypto = new Crypto();
                        newCrypto.setSymbol(symbol);
                        newCrypto.setName(name);
                        return newCrypto;
                    });

            crypto.setCurrentPrice(new java.math.BigDecimal(price));
            cryptoRepository.save(crypto);
        }
        
        // Broadcast new market data to everyone globally
        messagingTemplate.convertAndSend("/topic/market", coins);

        System.out.println("Crypto prices updated & broadcasted automatically");

        // Broadcast personalized portfolio alerts and updates to users
        List<User> users = userRepository.findAll();
        for (User user : users) {
             try {
                DashboardResponseDTO dash = dashboardService.getDashboard(user.getEmail());
                messagingTemplate.convertAndSend("/topic/alerts/" + user.getEmail(), dash);
             } catch (Exception e) {
                // Ignore for users without wallets
             }
        }
    }
}