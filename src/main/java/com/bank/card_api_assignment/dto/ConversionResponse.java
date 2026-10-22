package com.bank.card_api_assignment.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class ConversionResponse {
    private String refId;
    private String merchantName;
    private BigDecimal originalAmount;
    private String originalCurrency;
    private String targetCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private String rateSource;
}
