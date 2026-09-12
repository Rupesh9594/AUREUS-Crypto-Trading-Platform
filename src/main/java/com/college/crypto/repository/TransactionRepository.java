package com.college.crypto.repository;

import com.college.crypto.entity.Transaction;
import com.college.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser(User user);

}