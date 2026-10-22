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
    private Long id;                       

    @Column(name = "ref_id", nullable = false, unique = true, length = 50)
    private String refId;                  

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;             

    @Column(nullable = false, length = 3)
    private String currency;               

    @Column(nullable = false, length = 20)
    private String status;                 

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist                            
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}