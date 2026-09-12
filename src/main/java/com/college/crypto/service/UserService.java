package com.college.crypto.service;

import com.college.crypto.dto.UserRequestDTO;
import com.college.crypto.dto.UserResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    UserResponseDTO saveUser(UserRequestDTO requestDTO);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long id);

    UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO);

    void deleteUser(Long id);

    // ✅ Profile methods
    UserResponseDTO getProfile(String email);
    UserResponseDTO updateProfile(String email, UserRequestDTO requestDTO);

    // ✅ Pagination method (MATCHES implementation)
    Page<UserResponseDTO> getUsersWithPagination(
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}