package com.RotaDurak.RotaDurak.service;
import com.RotaDurak.RotaDurak.model.BankCard;
import com.RotaDurak.RotaDurak.model.User;
import com.RotaDurak.RotaDurak.repository.BankCardRepository;
import com.RotaDurak.RotaDurak.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class BankCardService {
    @Autowired
    private BankCardRepository bankCardRepository;
    @Autowired
    private UserRepository userRepository;

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

    private String generateCardNumber() {
        // Başına 9999 koy (internal sanal kart prefix)
        return "9999" + String.format("%012d", new Random().nextLong() % 1_000_000_000_000L).replace("-","");
    }

    public BankCard createVirtualCard(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        BankCard virtualCard = new BankCard();
        virtualCard.setUser(user);
        virtualCard.setCardType("VIRTUAL");
        virtualCard.setNickname(nickname);
        virtualCard.setCardNumber(generateCardNumber());   // 16 haneli unique
        virtualCard.setQrCode(UUID.randomUUID().toString());
        virtualCard.setNfcToken(UUID.randomUUID().toString());
        virtualCard.setExpiryDate(LocalDateTime.now().plusYears(3));
        virtualCard.setCardProvider("INTERNAL");
        virtualCard.setIsActive(true);

        return bankCardRepository.save(virtualCard);
    }

    public BankCard loadBalanceToCard(Long cardId, Double amount) {
        if (amount <= 0) throw new RuntimeException("Geçersiz tutar.");

        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Kart bulunamadı."));

        card.setBalance(card.getBalance() + amount);
        return bankCardRepository.save(card);
    }

    public BankCard deductBalanceFromCard(Long cardId, Double amount) {
        if (amount <= 0) throw new RuntimeException("Geçersiz tutar.");

        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Kart bulunamadı."));

        if (card.getBalance() < amount) throw new RuntimeException("Yetersiz kart bakiyesi.");

        card.setBalance(card.getBalance() - amount);
        return bankCardRepository.save(card);
    }

    public BankCard getCardByNfcToken(String nfcToken) {
        return bankCardRepository.findByNfcToken(nfcToken)
                .orElseThrow(() -> new RuntimeException("Geçersiz NFC token."));
    }
}
