package com.college.crypto.controller;

import com.college.crypto.dto.*;
import com.college.crypto.entity.*;
import com.college.crypto.payload.ApiResponse;
import com.college.crypto.repository.*;
import com.college.crypto.service.DashboardService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CryptoRepository cryptoRepository;
    private final UserHoldingRepository userHoldingRepository;
    private final TransactionRepository transactionRepository;
    private final DashboardService dashboardService;

    public WalletController(WalletRepository walletRepository,
                            UserRepository userRepository,
                            CryptoRepository cryptoRepository,
                            UserHoldingRepository userHoldingRepository,
                            TransactionRepository transactionRepository,
                            DashboardService dashboardService) {

        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.cryptoRepository = cryptoRepository;
        this.userHoldingRepository = userHoldingRepository;
        this.transactionRepository = transactionRepository;
        this.dashboardService = dashboardService;
    }

    // ================= GET WALLET =================
    @GetMapping
    public ApiResponse<?> getWallet() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return new ApiResponse<>(
                true,
                "Wallet fetched successfully",
                new WalletResponseDTO(wallet.getBalance())
        );
    }

    // ================= DEPOSIT =================
    @PostMapping("/deposit")
    public ApiResponse<?> deposit(@RequestBody DepositRequest request) {

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            return new ApiResponse<>(false,
                    "Amount must be greater than zero",
                    null);
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        return new ApiResponse<>(true,
                "Amount deposited successfully",
                new WalletResponseDTO(wallet.getBalance()));
    }

    // ================= WITHDRAW =================
    @PostMapping("/withdraw")
    public ApiResponse<?> withdraw(@RequestBody DepositRequest request) {

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            return new ApiResponse<>(false,
                    "Amount must be greater than zero",
                    null);
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            return new ApiResponse<>(false,
                    "Insufficient balance",
                    null);
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        return new ApiResponse<>(true,
                "Amount withdrawn successfully",
                new WalletResponseDTO(wallet.getBalance()));
    }

    // ================= BUY CRYPTO =================
    @PostMapping("/buy")
    public ApiResponse<?> buyCrypto(@RequestBody BuyRequest request) {

        if (request.getQuantity() == null ||
                request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {

            return new ApiResponse<>(false,
                    "Invalid quantity",
                    null);
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Crypto crypto = cryptoRepository.findBySymbol(
                request.getSymbol().toUpperCase()
        ).orElseThrow(() -> new RuntimeException("Crypto not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal totalCost =
                crypto.getCurrentPrice().multiply(request.getQuantity());

        if (wallet.getBalance().compareTo(totalCost) < 0) {
            return new ApiResponse<>(false,
                    "Insufficient balance",
                    null);
        }

        wallet.setBalance(wallet.getBalance().subtract(totalCost));
        walletRepository.save(wallet);

        Optional<UserHolding> existingHolding =
                userHoldingRepository.findByUserAndCrypto(user, crypto);

        if (existingHolding.isPresent()) {

            UserHolding holding = existingHolding.get();

            BigDecimal oldInvestment =
                    holding.getAverageBuyPrice()
                            .multiply(holding.getQuantity());

            BigDecimal newInvestment =
                    crypto.getCurrentPrice()
                            .multiply(request.getQuantity());

            BigDecimal newQuantity =
                    holding.getQuantity().add(request.getQuantity());

            BigDecimal newAveragePrice =
                    oldInvestment.add(newInvestment)
                            .divide(newQuantity, 8, RoundingMode.HALF_UP);

            holding.setQuantity(newQuantity);
            holding.setAverageBuyPrice(newAveragePrice);

            userHoldingRepository.save(holding);

        } else {

            UserHolding newHolding =
                    new UserHolding(
                            user,
                            crypto,
                            request.getQuantity(),
                            crypto.getCurrentPrice()
                    );

            userHoldingRepository.save(newHolding);
        }

        Transaction transaction = new Transaction(
                "BUY",
                request.getQuantity(),
                crypto.getCurrentPrice(),
                totalCost,
                user,
                crypto
        );

        transactionRepository.save(transaction);

        return new ApiResponse<>(true,
                "Crypto purchased successfully",
                null);
    }

    // ================= SELL CRYPTO =================
    @PostMapping("/sell")
    public ApiResponse<?> sellCrypto(@RequestBody SellRequest request) {

        if (request.getQuantity() == null ||
                request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {

            return new ApiResponse<>(false,
                    "Invalid quantity",
                    null);
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Crypto crypto = cryptoRepository.findBySymbol(
                request.getSymbol().toUpperCase()
        ).orElseThrow(() -> new RuntimeException("Crypto not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        UserHolding holding = userHoldingRepository
                .findByUserAndCrypto(user, crypto)
                .orElseThrow(() -> new RuntimeException("You don't own this crypto"));

        if (holding.getQuantity().compareTo(request.getQuantity()) < 0) {
            return new ApiResponse<>(false,
                    "Insufficient crypto quantity",
                    null);
        }

        BigDecimal totalValue =
                crypto.getCurrentPrice().multiply(request.getQuantity());

        wallet.setBalance(wallet.getBalance().add(totalValue));
        walletRepository.save(wallet);

        holding.setQuantity(
                holding.getQuantity().subtract(request.getQuantity())
        );

        userHoldingRepository.save(holding);

        Transaction transaction = new Transaction(
                "SELL",
                request.getQuantity(),
                crypto.getCurrentPrice(),
                totalValue,
                user,
                crypto
        );

        transactionRepository.save(transaction);

        return new ApiResponse<>(true,
                "Crypto sold successfully",
                null);
    }

    // ================= PORTFOLIO =================
    // ================= PORTFOLIO =================
    @GetMapping("/portfolio")
    public ApiResponse<?> getPortfolio() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        List<PortfolioResponseDTO> portfolio =
                dashboardService.getPortfolio(email);

        return new ApiResponse<>(
                true,
                "Portfolio fetched successfully",
                portfolio
        );
    }


    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public ApiResponse<?> getDashboard() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        DashboardResponseDTO dashboard =
                dashboardService.getDashboard(email);

        return new ApiResponse<>(
                true,
                "Dashboard fetched successfully",
                dashboard
        );
    }

    @GetMapping("/profit")
    public ApiResponse<?> getCoinWiseProfit() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserHolding> holdings = userHoldingRepository.findByUser(user);

        List<ProfitResponseDTO> profit = holdings.stream().map(h -> {

            BigDecimal avgPrice = h.getAverageBuyPrice();
            BigDecimal currentPrice = h.getCrypto().getCurrentPrice();

            BigDecimal profitValue =
                    currentPrice.subtract(avgPrice)
                            .multiply(h.getQuantity());

            return new ProfitResponseDTO(
                    h.getCrypto().getSymbol(),
                    h.getQuantity(),
                    avgPrice,
                    currentPrice,
                    profitValue
            );

        }).toList();

        return new ApiResponse<>(
                true,
                "Coin-wise profit fetched",
                profit
        );
    }

}