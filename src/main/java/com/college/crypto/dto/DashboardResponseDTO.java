package com.college.crypto.dto;

import java.math.BigDecimal;

public class DashboardResponseDTO {

    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal profit;
    private BigDecimal profitPercentage;

    public DashboardResponseDTO(BigDecimal totalInvestment,
                                BigDecimal currentValue,
                                BigDecimal profit,
                                BigDecimal profitPercentage) {

        this.totalInvestment = totalInvestment;
        this.currentValue = currentValue;
        this.profit = profit;
        this.profitPercentage = profitPercentage;
    }

    public BigDecimal getTotalInvestment() {
        return totalInvestment;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public BigDecimal getProfitPercentage() {
        return profitPercentage;
    }
}