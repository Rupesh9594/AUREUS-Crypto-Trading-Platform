package com.college.crypto.repository;

import com.college.crypto.entity.PendingOrder;
import com.college.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PendingOrderRepository extends JpaRepository<PendingOrder, Long> {
    List<PendingOrder> findByUser(User user);
}
