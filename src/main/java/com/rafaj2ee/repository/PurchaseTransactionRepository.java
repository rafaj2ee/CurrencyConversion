package com.rafaj2ee.repository;

import com.rafaj2ee.model.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
    List<PurchaseTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
