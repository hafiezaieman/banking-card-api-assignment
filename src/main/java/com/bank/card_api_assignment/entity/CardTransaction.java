package com.bank.card_api_assignment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_transactions")
@Getter
@Setter
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                       // internal database key

    @Column(name = "ref_id", nullable = false, unique = true, length = 50)
    private String refId;                  // business reference used by clients

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;             // BigDecimal for money, never double

    @Column(nullable = false, length = 3)
    private String currency;               // e.g. MYR, USD

    @Column(nullable = false, length = 20)
    private String status;                 // e.g. PENDING, SUCCESS, FAILED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist                            // runs automatically before the first INSERT
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}