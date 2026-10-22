package com.bank.card_api_assignment.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class TransactionUpdateRequest {
    @NotBlank @Size(max = 100) private String merchantName;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    @NotBlank @Size(min = 3, max = 3) private String currency;
    @NotBlank @Size(max = 20)  private String status;
}