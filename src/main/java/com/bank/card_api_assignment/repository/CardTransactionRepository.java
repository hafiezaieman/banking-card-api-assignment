package com.bank.card_api_assignment.repository;

import com.bank.card_api_assignment.entity.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {
    Optional<CardTransaction> findByRefId(String refId);
    boolean existsByRefId(String refId);
}
