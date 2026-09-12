package com.college.crypto.controller;

import com.college.crypto.entity.Crypto;
import com.college.crypto.entity.PendingOrder;
import com.college.crypto.entity.User;
import com.college.crypto.payload.ApiResponse;
import com.college.crypto.repository.CryptoRepository;
import com.college.crypto.repository.PendingOrderRepository;
import com.college.crypto.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PendingOrderRepository pendingOrderRepository;
    private final UserRepository userRepository;
    private final CryptoRepository cryptoRepository;

    public OrderController(PendingOrderRepository pendingOrderRepository, UserRepository userRepository, CryptoRepository cryptoRepository) {
        this.pendingOrderRepository = pendingOrderRepository;
        this.userRepository = userRepository;
        this.cryptoRepository = cryptoRepository;
    }

    @GetMapping
    public ApiResponse<?> getOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        List<Map<String, Object>> res = pendingOrderRepository.findByUser(user).stream().map(o -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", o.getId());
            map.put("type", o.getType());
            map.put("symbol", o.getCrypto().getSymbol());
            map.put("quantity", o.getQuantity());
            map.put("targetPrice", o.getTargetPrice());
            return map;
        }).collect(Collectors.toList());

        return new ApiResponse<>(true, "Orders fetched", res);
    }

    @PostMapping("/limit")
    public ApiResponse<?> placeLimitOrder(@RequestBody Map<String, String> body) {
        String type = body.get("type").toUpperCase();
        String symbol = body.get("symbol").toUpperCase();
        BigDecimal quantity = new BigDecimal(body.get("quantity"));
        BigDecimal targetPrice = new BigDecimal(body.get("targetPrice"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Crypto crypto = cryptoRepository.findBySymbol(symbol).orElseThrow();

        PendingOrder order = new PendingOrder(type, user, crypto, quantity, targetPrice);
        pendingOrderRepository.save(order);

        return new ApiResponse<>(true, "Limit order placed successfully", null);
    }

    @PostMapping("/cancel")
    public ApiResponse<?> cancelOrder(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        pendingOrderRepository.findById(id).ifPresent(pendingOrderRepository::delete);
        return new ApiResponse<>(true, "Order cancelled", null);
    }
}
