package com.RotaDurak.RotaDurak.controller;
import com.RotaDurak.RotaDurak.model.BankCard;
import com.RotaDurak.RotaDurak.service.BankCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bankcards")
public class BankCardController {
    @Autowired
    private BankCardService bankCardService;

    @GetMapping
    public List<BankCard> getAllCards() {
        return bankCardService.getAllCards();
    }

    @GetMapping("/{id}")
    public Optional<BankCard> getCardById(@PathVariable Long id) {
        return bankCardService.getCardById(id);
    }

    @GetMapping("/user/{userId}")
    public List<BankCard> getCardsByUserId(@PathVariable Long id) {
        return bankCardService.getCardsByUserId(id);
    }

    @PostMapping
    public BankCard createCard(@RequestBody BankCard bankCard) {
        return bankCardService.createCard(bankCard);
    }

    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable Long id) {
        bankCardService.deleteCard(id);
    }
}
