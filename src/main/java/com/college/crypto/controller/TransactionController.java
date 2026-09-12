package com.college.crypto.controller;

import com.college.crypto.dto.TransactionResponseDTO;
import com.college.crypto.entity.Transaction;
import com.college.crypto.entity.User;
import com.college.crypto.payload.ApiResponse;
import com.college.crypto.repository.TransactionRepository;
import com.college.crypto.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionController(TransactionRepository transactionRepository,
                                 UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<?> getTransactions() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUser(user);

        List<TransactionResponseDTO> response = transactions.stream()
                .map(t -> new TransactionResponseDTO(
                        t.getType(),
                        t.getCrypto().getSymbol(),
                        t.getQuantity(),
                        t.getPrice(),
                        t.getTotalAmount()
                ))
                .toList();

        return new ApiResponse<>(
                true,
                "Transaction history fetched",
                response
        );
    }
}