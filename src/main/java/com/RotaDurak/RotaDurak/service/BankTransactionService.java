package com.RotaDurak.RotaDurak.service;
import com.RotaDurak.RotaDurak.model.BankTransaction;
import com.RotaDurak.RotaDurak.repository.BankTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BankTransactionService {
    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    public List<BankTransaction> getAllTransactions() {
        return bankTransactionRepository.findAll();
    }

    public Optional<BankTransaction> getTransactionById(Long id) {
        return bankTransactionRepository.findById(id);
    }

    public List<BankTransaction> getTransactionsByUserId(Long id) {
        return bankTransactionRepository.findByUserId(id);
    }

    public BankTransaction createTransaction(BankTransaction transaction) {
        return bankTransactionRepository.save(transaction);
    }
}
