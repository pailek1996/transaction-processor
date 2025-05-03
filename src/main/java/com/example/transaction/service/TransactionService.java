package com.example.transaction.service;

import com.example.transaction.model.Transaction;
import com.example.transaction.repository.TransactionRepository;
import com.example.transaction.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;

    public Page<Transaction> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Transaction> findByAccountNumber(String accountNumber, Pageable pageable) {
        return repository.findByAccountNumber(accountNumber, pageable);
    }

    public Page<Transaction> findByCustomerId(String customerId, Pageable pageable) {
        return repository.findByCustomerId(customerId, pageable);
    }

    public Page<Transaction> findByDescription(String description, Pageable pageable) {
        return repository.findByDescriptionContaining(description, pageable);
    }

    public Transaction findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    public Transaction save(Transaction transaction) {
        return repository.save(transaction);
    }

    @Transactional
    public Transaction update(Long id, Transaction transactionDetails) {
        Transaction transaction = findById(id);

        transaction.setAccountNumber(transactionDetails.getAccountNumber());
        transaction.setTrxAmount(transactionDetails.getTrxAmount());
        transaction.setDescription(transactionDetails.getDescription());
        transaction.setTrxDate(transactionDetails.getTrxDate());
        transaction.setTrxTime(transactionDetails.getTrxTime());
        transaction.setCustomerId(transactionDetails.getCustomerId());

        return repository.save(transaction);
    }
}
