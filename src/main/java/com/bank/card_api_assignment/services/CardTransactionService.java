package com.bank.card_api_assignment.services;

import com.bank.card_api_assignment.dto.*;
import com.bank.card_api_assignment.entity.CardTransaction;
import com.bank.card_api_assignment.exception.*;
import com.bank.card_api_assignment.repository.CardTransactionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardTransactionService {

    private static final int PAGE_SIZE = 10;
    private final CardTransactionRepository repository;

    @Transactional                                   // INSERT
    public TransactionResponse create(TransactionRequest req) {
        if (repository.existsByRefId(req.getRefId())) {
            throw new DuplicateResourceException("refId already exists: " + req.getRefId());
        }
        CardTransaction tx = new CardTransaction();
        tx.setRefId(req.getRefId());
        tx.setMerchantName(req.getMerchantName());
        tx.setAmount(req.getAmount());
        tx.setCurrency(req.getCurrency().toUpperCase());
        tx.setStatus(req.getStatus().toUpperCase());
        return toResponse(repository.save(tx));
    }

    @Transactional(readOnly = true)                  // GET one
    public TransactionResponse getByRefId(String refId) {
        return toResponse(findOrThrow(refId));
    }

    @Transactional(readOnly = true)                  // GET list with pagination
    public PageResponse<TransactionResponse> getAll(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        Page<CardTransaction> result = repository.findAll(pageable);
        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional                                   // UPDATE
    public TransactionResponse update(String refId, TransactionUpdateRequest req) {
        CardTransaction tx = findOrThrow(refId);
        tx.setMerchantName(req.getMerchantName());
        tx.setAmount(req.getAmount());
        tx.setCurrency(req.getCurrency().toUpperCase());
        tx.setStatus(req.getStatus().toUpperCase());
        return toResponse(repository.save(tx));
    }

    private CardTransaction findOrThrow(String refId) {
        return repository.findByRefId(refId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + refId));
    }

    private TransactionResponse toResponse(CardTransaction tx) {
        TransactionResponse r = new TransactionResponse();
        r.setRefId(tx.getRefId());
        r.setMerchantName(tx.getMerchantName());
        r.setAmount(tx.getAmount());
        r.setCurrency(tx.getCurrency());
        r.setStatus(tx.getStatus());
        r.setCreatedAt(tx.getCreatedAt());
        return r;
    }
}