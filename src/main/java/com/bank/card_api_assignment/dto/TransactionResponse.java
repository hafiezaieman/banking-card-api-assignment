package com.bank.card_api_assignment.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class TransactionResponse {
    private String refId;
    private String merchantName;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
