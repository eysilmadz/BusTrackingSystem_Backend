package com.RotaDurak.RotaDurak.controller;
import com.RotaDurak.RotaDurak.model.BankTransaction;
import com.RotaDurak.RotaDurak.service.BankTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class BankTransactionController {
    @Autowired
    private BankTransactionService bankTransactionService;

    @GetMapping
    public List<BankTransaction> getAllTransactions() {
        return bankTransactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public Optional<BankTransaction> getTransactionById(@PathVariable Long id) {
        return bankTransactionService.getTransactionById(id);
    }

    @GetMapping("/user/{userId}")
    public List<BankTransaction> getTransactionsByUserId(@PathVariable Long id) {
        return bankTransactionService.getTransactionsByUserId(id);
    }

    @PostMapping
    public BankTransaction createTransaction(@RequestBody BankTransaction transaction) {
        return bankTransactionService.createTransaction(transaction);
    }
}
