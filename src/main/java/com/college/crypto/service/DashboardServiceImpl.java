package com.college.crypto.service;

import com.college.crypto.dto.DashboardResponseDTO;
import com.college.crypto.dto.PortfolioResponseDTO;
import com.college.crypto.entity.User;
import com.college.crypto.entity.UserHolding;
import com.college.crypto.repository.UserHoldingRepository;
import com.college.crypto.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final UserHoldingRepository userHoldingRepository;

    public DashboardServiceImpl(UserRepository userRepository,
                                UserHoldingRepository userHoldingRepository) {

        this.userRepository = userRepository;
        this.userHoldingRepository = userHoldingRepository;
    }

    // ================= PORTFOLIO =================
    @Override
    public List<PortfolioResponseDTO> getPortfolio(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserHolding> holdings = userHoldingRepository.findByUser(user);

        return holdings.stream().map(h -> {

            BigDecimal currentValue =
                    h.getCrypto().getCurrentPrice()
                            .multiply(h.getQuantity());

            return new PortfolioResponseDTO(
                    h.getCrypto().getSymbol(),
                    h.getQuantity(),
                    h.getCrypto().getCurrentPrice(),
                    currentValue
            );

        }).toList();
    }

    // ================= DASHBOARD =================
    @Override
    public DashboardResponseDTO getDashboard(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserHolding> holdings = userHoldingRepository.findByUser(user);

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;

        for (UserHolding h : holdings) {

            BigDecimal investment =
                    h.getAverageBuyPrice().multiply(h.getQuantity());

            BigDecimal value =
                    h.getCrypto().getCurrentPrice().multiply(h.getQuantity());

            totalInvestment = totalInvestment.add(investment);
            currentValue = currentValue.add(value);
        }

        BigDecimal profit = currentValue.subtract(totalInvestment);

        BigDecimal profitPercentage = BigDecimal.ZERO;

        if (totalInvestment.compareTo(BigDecimal.ZERO) > 0) {
            profitPercentage = profit
                    .divide(totalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new DashboardResponseDTO(
                totalInvestment,
                currentValue,
                profit,
                profitPercentage
        );
    }
}