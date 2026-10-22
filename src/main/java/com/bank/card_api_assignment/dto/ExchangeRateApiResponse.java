package com.bank.card_api_assignment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Map;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true) 
public class ExchangeRateApiResponse {
    private String result;
    @JsonProperty("base_code")
    private String baseCode;
    private Map<String, BigDecimal> rates;
}
