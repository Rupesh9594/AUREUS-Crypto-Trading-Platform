package com.college.crypto.repository;

import com.college.crypto.entity.UserHolding;
import com.college.crypto.entity.User;
import com.college.crypto.entity.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserHoldingRepository extends JpaRepository<UserHolding, Long> {

    Optional<UserHolding> findByUserAndCrypto(User user, Crypto crypto);

    List<UserHolding> findByUser(User user);
}