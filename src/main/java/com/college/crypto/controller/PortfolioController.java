package com.college.crypto.controller;

import com.college.crypto.dto.PortfolioResponseDTO;
import com.college.crypto.entity.User;
import com.college.crypto.entity.UserHolding;
import com.college.crypto.payload.ApiResponse;
import com.college.crypto.repository.UserHoldingRepository;
import com.college.crypto.repository.UserRepository;
import com.college.crypto.dto.ProfitResponseDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final UserHoldingRepository userHoldingRepository;
    private final UserRepository userRepository;

    public PortfolioController(UserHoldingRepository userHoldingRepository,
                               UserRepository userRepository) {
        this.userHoldingRepository = userHoldingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<?> getPortfolio() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserHolding> holdings = userHoldingRepository.findByUser(user);

        List<PortfolioResponseDTO> portfolio = holdings.stream()
                .map(h -> {

                    BigDecimal currentPrice = h.getCrypto().getCurrentPrice();
                    BigDecimal totalValue =
                            currentPrice.multiply(h.getQuantity());

                    return new PortfolioResponseDTO(
                            h.getCrypto().getSymbol(),
                            h.getQuantity(),
                            currentPrice,
                            totalValue
                    );

                }).collect(Collectors.toList());

        return new ApiResponse<>(
                true,
                "Portfolio fetched successfully",
                portfolio
        );
    }

    @GetMapping("/profit")
    public ApiResponse<?> getProfitLoss() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserHolding> holdings = userHoldingRepository.findByUser(user);

        List<ProfitResponseDTO> profits = holdings.stream().map(h -> {

            BigDecimal buyPrice = h.getCrypto().getCurrentPrice();
            BigDecimal currentPrice = h.getCrypto().getCurrentPrice();

            BigDecimal profit =
                    currentPrice.subtract(buyPrice)
                            .multiply(h.getQuantity());

            return new ProfitResponseDTO(
                    h.getCrypto().getSymbol(),
                    h.getQuantity(),
                    buyPrice,
                    currentPrice,
                    profit
            );

        }).toList();

        return new ApiResponse<>(
                true,
                "Profit/Loss calculated successfully",
                profits
        );
    }
}