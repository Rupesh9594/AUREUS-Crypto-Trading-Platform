package com.college.crypto.controller;

import com.college.crypto.dto.UserRequestDTO;
import com.college.crypto.dto.UserResponseDTO;
import com.college.crypto.payload.ApiResponse;
import com.college.crypto.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final com.college.crypto.service.DashboardService dashboardService;
    private final com.college.crypto.repository.UserRepository userRepository;

    public UserController(UserService userService, 
                          com.college.crypto.service.DashboardService dashboardService,
                          com.college.crypto.repository.UserRepository userRepository) {
        this.userService = userService;
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getLeaderboard() {
        List<java.util.Map<String, Object>> leaderboard = userRepository.findAll().stream()
            .map(user -> {
                com.college.crypto.dto.DashboardResponseDTO d = dashboardService.getDashboard(user.getEmail());
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("name", user.getName());
                map.put("totalProfit", d.getProfit());
                map.put("photo", user.getProfilePhotoUrl());
                return map;
            })
            .sorted((java.util.Map<String, Object> a, java.util.Map<String, Object> b) -> 
                 ((java.math.BigDecimal)b.get("totalProfit")).compareTo((java.math.BigDecimal)a.get("totalProfit")))
            .limit(10)
            .collect(java.util.stream.Collectors.toList());
        return new ResponseEntity<>(new ApiResponse<>(true, "Leaderboard fetched", leaderboard), HttpStatus.OK);
    }

    // ✅ PROFILE ENDPOINTS
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getProfile() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        UserResponseDTO profile = userService.getProfile(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile fetched", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(@RequestBody UserRequestDTO request) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        UserResponseDTO updated = userService.updateProfile(email, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updated));
    }

    // ✅ CREATE USER
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @RequestBody UserRequestDTO requestDTO) {

        UserResponseDTO responseDTO = userService.saveUser(requestDTO);

        ApiResponse<UserResponseDTO> response =
                new ApiResponse<>(true, "User created successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ✅ GET ALL USERS (WITHOUT PAGINATION)
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {

        List<UserResponseDTO> users = userService.getAllUsers();

        ApiResponse<List<UserResponseDTO>> response =
                new ApiResponse<>(true, "Users fetched successfully", users);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ✅ GET USER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            @PathVariable Long id) {

        UserResponseDTO user = userService.getUserById(id);

        ApiResponse<UserResponseDTO> response =
                new ApiResponse<>(true, "User fetched successfully", user);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ✅ UPDATE USER
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequestDTO requestDTO) {

        UserResponseDTO updatedUser =
                userService.updateUser(id, requestDTO);

        ApiResponse<UserResponseDTO> response =
                new ApiResponse<>(true, "User updated successfully", updatedUser);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ✅ DELETE USER
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        ApiResponse<String> response =
                new ApiResponse<>(true, "User deleted successfully", null);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ✅ PAGINATION + SORTING
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getUsersWithPagination(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<UserResponseDTO> pagedUsers =
                userService.getUsersWithPagination(page, size, sortBy, sortDir);

        ApiResponse<Page<UserResponseDTO>> response =
                new ApiResponse<>(true, "Users fetched successfully", pagedUsers);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}