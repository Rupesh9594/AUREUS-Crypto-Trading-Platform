package com.college.crypto.auth;

import com.college.crypto.payload.ApiResponse;
import com.college.crypto.entity.User;
import com.college.crypto.entity.Wallet;
import com.college.crypto.repository.UserRepository;
import com.college.crypto.repository.WalletRepository;
import com.college.crypto.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          WalletRepository walletRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Email already exists", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Strict 18+ Age Validation
        if (request.getDateOfBirth() != null) {
            LocalDate today = LocalDate.now();
            int age = Period.between(request.getDateOfBirth(), today).getYears();
            if (age < 18) {
                return new ResponseEntity<>(
                        new ApiResponse<>(false, "Registration restricted: You must be at least 18 years old.", null),
                        HttpStatus.BAD_REQUEST
                );
            }
        } else {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Date of Birth is required.", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setCountry(request.getCountry());
        user.setGender(request.getGender());

        User savedUser = userRepository.save(user);

        // 🔥 CREATE WALLET WITH INITIAL BALANCE 10000
        Wallet wallet = new Wallet(new BigDecimal("10000"), savedUser);
        walletRepository.save(wallet);

        return new ResponseEntity<>(
                new ApiResponse<>(true, "User registered successfully", null),
                HttpStatus.OK
        );
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Invalid credentials", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 🔥 GENERATE TOKEN
        String token = jwtService.generateToken(user.getEmail());

        return new ResponseEntity<>(
                new ApiResponse<>(true, "Login successful", token),
                HttpStatus.OK
        );
    }
}