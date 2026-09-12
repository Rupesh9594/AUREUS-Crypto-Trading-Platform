package com.college.crypto.service;

import com.college.crypto.dto.DashboardResponseDTO;
import com.college.crypto.dto.PortfolioResponseDTO;

import java.util.List;

public interface DashboardService {

    List<PortfolioResponseDTO> getPortfolio(String email);

    DashboardResponseDTO getDashboard(String email);
}