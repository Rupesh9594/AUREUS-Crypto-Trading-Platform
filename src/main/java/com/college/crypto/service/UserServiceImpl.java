package com.college.crypto.service;

import com.college.crypto.dto.UserRequestDTO;
import com.college.crypto.dto.UserResponseDTO;
import com.college.crypto.dto.DashboardResponseDTO;
import com.college.crypto.entity.User;
import com.college.crypto.exception.UserNotFoundException;
import com.college.crypto.repository.UserRepository;
import com.college.crypto.service.DashboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DashboardService dashboardService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           DashboardService dashboardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dashboardService = dashboardService;
    }

    private UserResponseDTO mapToResponse(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBio(),
                user.getPhoneNumber(),
                user.getProfilePhotoUrl(),
                user.getThemePreference(),
                user.getUserTier(),
                user.getJoinedAt(),
                user.getDateOfBirth(),
                user.getCountry(),
                user.getGender(),
                user.isKycVerified()
        );
    }

    private String calculateTier(BigDecimal portfolioValue) {
        if (portfolioValue.compareTo(BigDecimal.valueOf(100000)) >= 0) {
            return "Aureus Prime";
        } else if (portfolioValue.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            return "Pro";
        } else {
            return "Novice";
        }
    }

    @Override
    public UserResponseDTO saveUser(UserRequestDTO requestDTO) {
        // Strict 18+ Age Validation
        if (requestDTO.getDateOfBirth() != null) {
            LocalDate today = LocalDate.now();
            int age = Period.between(requestDTO.getDateOfBirth(), today).getYears();
            if (age < 18) {
                throw new RuntimeException("Registration restricted: You must be at least 18 years old to trade on AUREUS.");
            }
        } else {
            throw new RuntimeException("Date of Birth is required for verification.");
        }

        User user = new User();
        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setDateOfBirth(requestDTO.getDateOfBirth());
        user.setCountry(requestDTO.getCountry());
        user.setGender(requestDTO.getGender());

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with ID: " + id));

        return mapToResponse(user);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with ID: " + id));

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with ID: " + id));

        userRepository.delete(user);
    }

    @Override
    public UserResponseDTO getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        
        // Update Tier dynamically
        DashboardResponseDTO d = dashboardService.getDashboard(email);
        user.setUserTier(calculateTier(d.getCurrentValue()));
        userRepository.save(user);

        return mapToResponse(user);
    }

    @Override
    public UserResponseDTO updateProfile(String email, UserRequestDTO requestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        
        if (requestDTO.getName() != null) user.setName(requestDTO.getName());
        if (requestDTO.getBio() != null) user.setBio(requestDTO.getBio());
        if (requestDTO.getPhoneNumber() != null) user.setPhoneNumber(requestDTO.getPhoneNumber());
        if (requestDTO.getProfilePhotoUrl() != null) user.setProfilePhotoUrl(requestDTO.getProfilePhotoUrl());
        if (requestDTO.getThemePreference() != null) user.setThemePreference(requestDTO.getThemePreference());
        if (requestDTO.getCountry() != null) user.setCountry(requestDTO.getCountry());
        if (requestDTO.getGender() != null) user.setGender(requestDTO.getGender());
        
        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    // ✅ Correct pagination method
    @Override
    public Page<UserResponseDTO> getUsersWithPagination(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }
}