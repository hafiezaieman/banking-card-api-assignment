// service/CurrencyConversionService.java
package com.bank.card_api_assignment.services;

import com.bank.card_api_assignment.dto.*;
import com.bank.card_api_assignment.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CurrencyConversionService {

    private static final String RATE_URL = "https://open.er-api.com/v6/latest/";

    private final CardTransactionService transactionService;
    private final RestTemplate restTemplate;

    public ConversionResponse convert(String refId, String targetCurrency) {
        TransactionResponse tx = transactionService.getByRefId(refId);   

        String target = targetCurrency.toUpperCase();
        ExchangeRateApiResponse api;
        try {
            api = restTemplate.getForObject(RATE_URL + tx.getCurrency(), ExchangeRateApiResponse.class);
        } catch (RestClientException ex) {
            throw new ExternalApiException("Exchange rate service unavailable: " + ex.getMessage());
        }

        if (api == null || !"success".equals(api.getResult()) || api.getRates() == null) {
            throw new ExternalApiException("Exchange rate service returned an invalid response");
        }
        BigDecimal rate = api.getRates().get(target);
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported target currency: " + target);
        }

        ConversionResponse r = new ConversionResponse();             
        r.setRefId(tx.getRefId());
        r.setMerchantName(tx.getMerchantName());
        r.setOriginalAmount(tx.getAmount());
        r.setOriginalCurrency(tx.getCurrency());
        r.setTargetCurrency(target);
        r.setExchangeRate(rate);
        r.setConvertedAmount(tx.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP));
        r.setRateSource("open.er-api.com");
        return r;
    }
}