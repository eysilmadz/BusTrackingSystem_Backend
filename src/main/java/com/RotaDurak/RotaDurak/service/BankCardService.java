package com.RotaDurak.RotaDurak.service;
import com.RotaDurak.RotaDurak.model.BankCard;
import com.RotaDurak.RotaDurak.repository.BankCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BankCardService {
    @Autowired
    private BankCardRepository bankCardRepository;

    public List<BankCard> getAllCards() {
        return bankCardRepository.findAll();
    }

    public Optional<BankCard> getCardById(Long id) {
        return bankCardRepository.findById(id);
    }

    public List<BankCard> getCardsByUserId(Long id) {
        return bankCardRepository.findByUserId(id);
    }

    public BankCard createCard(BankCard bankCard) {
        return bankCardRepository.save(bankCard);
    }

    public void deleteCard(Long id) {
        bankCardRepository.deleteById(id);
    }
}
