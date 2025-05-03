package com.example.transaction.batch;

import com.example.transaction.model.Transaction;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TransactionProcessor implements ItemProcessor<Transaction, Transaction> {

    @Override
    public Transaction process(Transaction transaction) throws Exception {
        return transaction;
    }
}
