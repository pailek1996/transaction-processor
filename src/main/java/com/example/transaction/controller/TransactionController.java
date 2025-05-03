package com.example.transaction.controller;

import com.example.transaction.model.Transaction;
import com.example.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<Transaction>> getAllTransactions(Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.findById(id));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<Page<Transaction>> getTransactionsByAccountNumber(
            @PathVariable String accountNumber, Pageable pageable) {
        return ResponseEntity.ok(transactionService.findByAccountNumber(accountNumber, pageable));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<Transaction>> getTransactionsByCustomerId(
            @PathVariable String customerId, Pageable pageable) {
        return ResponseEntity.ok(transactionService.findByCustomerId(customerId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Transaction>> getTransactionsByDescription(
            @RequestParam String description, Pageable pageable) {
        return ResponseEntity.ok(transactionService.findByDescription(description, pageable));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        Transaction savedTransaction = transactionService.save(transaction);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedTransaction.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedTransaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Long id, @Valid @RequestBody Transaction transaction) {
        Transaction updatedTransaction = transactionService.update(id, transaction);
        return ResponseEntity.ok(updatedTransaction);
    }
}