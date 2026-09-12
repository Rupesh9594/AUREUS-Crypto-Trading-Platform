package com.college.crypto.scheduler;

import com.college.crypto.entity.*;
import com.college.crypto.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Component
public class OrderExecutionScheduler {

    private final PendingOrderRepository pendingOrderRepository;
    private final WalletRepository walletRepository;
    private final UserHoldingRepository userHoldingRepository;
    private final TransactionRepository transactionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public OrderExecutionScheduler(PendingOrderRepository pendingOrderRepository, WalletRepository walletRepository, UserHoldingRepository userHoldingRepository, TransactionRepository transactionRepository, SimpMessagingTemplate messagingTemplate) {
        this.pendingOrderRepository = pendingOrderRepository;
        this.walletRepository = walletRepository;
        this.userHoldingRepository = userHoldingRepository;
        this.transactionRepository = transactionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Runs every 30s, slightly offset from price update
    @Scheduled(initialDelay = 5000, fixedRate = 30000)
    public void executeOrders() {
        List<PendingOrder> orders = pendingOrderRepository.findAll();
        for (PendingOrder order : orders) {
            Crypto crypto = order.getCrypto();
            BigDecimal currentPrice = crypto.getCurrentPrice();
            
            boolean execute = false;
            
            if (order.getType().equalsIgnoreCase("BUY") && currentPrice.compareTo(order.getTargetPrice()) <= 0) {
                execute = true;
            } else if (order.getType().equalsIgnoreCase("SELL") && currentPrice.compareTo(order.getTargetPrice()) >= 0) {
                execute = true;
            }

            if (execute) {
                processTrade(order, currentPrice);
            }
        }
    }

    private void processTrade(PendingOrder order, BigDecimal executionPrice) {
        User user = order.getUser();
        Wallet wallet = walletRepository.findByUser(user).orElse(null);
        if (wallet == null) return;

        BigDecimal totalCost = executionPrice.multiply(order.getQuantity());

        if (order.getType().equalsIgnoreCase("BUY")) {
            if (wallet.getBalance().compareTo(totalCost) >= 0) {
                wallet.setBalance(wallet.getBalance().subtract(totalCost));
                walletRepository.save(wallet);

                UserHolding holding = userHoldingRepository.findByUserAndCrypto(user, order.getCrypto()).orElse(new UserHolding(user, order.getCrypto(), BigDecimal.ZERO, executionPrice));
                
                BigDecimal oldInvestment = holding.getAverageBuyPrice().multiply(holding.getQuantity());
                BigDecimal newQuantity = holding.getQuantity().add(order.getQuantity());
                holding.setAverageBuyPrice(oldInvestment.add(totalCost).divide(newQuantity, 8, RoundingMode.HALF_UP));
                holding.setQuantity(newQuantity);
                userHoldingRepository.save(holding);

                transactionRepository.save(new Transaction("BUY_LIMIT", order.getQuantity(), executionPrice, totalCost, user, order.getCrypto()));
                pendingOrderRepository.delete(order);

                messagingTemplate.convertAndSend("/topic/alerts/" + user.getEmail(), "LIMIT BUY Executed target: " + order.getCrypto().getSymbol());
            }
        } else {
            Optional<UserHolding> holdingOpt = userHoldingRepository.findByUserAndCrypto(user, order.getCrypto());
            if (holdingOpt.isPresent()) {
                UserHolding holding = holdingOpt.get();
                if (holding.getQuantity().compareTo(order.getQuantity()) >= 0) {
                    wallet.setBalance(wallet.getBalance().add(totalCost));
                    walletRepository.save(wallet);

                    holding.setQuantity(holding.getQuantity().subtract(order.getQuantity()));
                    userHoldingRepository.save(holding);

                    transactionRepository.save(new Transaction("SELL_LIMIT", order.getQuantity(), executionPrice, totalCost, user, order.getCrypto()));
                    pendingOrderRepository.delete(order);

                    messagingTemplate.convertAndSend("/topic/alerts/" + user.getEmail(), "LIMIT SELL Executed target: " + order.getCrypto().getSymbol());
                }
            }
        }
    }
}
