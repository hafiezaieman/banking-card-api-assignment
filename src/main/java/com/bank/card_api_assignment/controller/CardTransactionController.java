package com.bank.card_api_assignment.controller;

import com.bank.card_api_assignment.dto.*;
import com.bank.card_api_assignment.services.CardTransactionService;
import com.bank.card_api_assignment.services.CurrencyConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class CardTransactionController {

    private final CardTransactionService service;
    private final CurrencyConversionService conversionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping("/{refId}")
    public TransactionResponse getOne(@PathVariable String refId) {
        return service.getByRefId(refId);
    }

    @GetMapping
    public PageResponse<TransactionResponse> getAll(@RequestParam(defaultValue = "0") int page) {
        return service.getAll(Math.max(page, 0));
    }

    @PutMapping("/{refId}")
    public TransactionResponse update(@PathVariable String refId,
                                      @Valid @RequestBody TransactionUpdateRequest req) {
        return service.update(refId, req);
    }

    @GetMapping("/{refId}/convert")
    public ConversionResponse convert(@PathVariable String refId, @RequestParam String to) {
        return conversionService.convert(refId, to);
    }
}