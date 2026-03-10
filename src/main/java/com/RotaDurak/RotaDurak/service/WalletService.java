package com.RotaDurak.RotaDurak.service;

import com.RotaDurak.RotaDurak.model.User;
import com.RotaDurak.RotaDurak.model.Wallet;
import com.RotaDurak.RotaDurak.repository.BankTransactionRepository;
import com.RotaDurak.RotaDurak.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    //Kullanıcı oluşturulunca otomatik cüzdan oluştur
    public Wallet createWallet(User user) {
        if (walletRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Bu kullanıcının zaten bir cüzdanı var.");
        }
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(0.0)
                .currency("TRY")
                .isActive(true)
                .build();
        return walletRepository.save(wallet);
    }

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Cüzdan bulunamadı."));
    }

    // Para yükleme (İYZİCO'dan başarılı callback sonrası çağrılır)
    public Wallet loadBalance(Long userId, Double amount) {
        if (amount <= 0) throw new RuntimeException("Geçersiz tutar.");

        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        return walletRepository.save(wallet);
    }

    // Otobüs ödemesi (NFC/QR tetikler)
    public Wallet deductBalance(Long userId, Double amount) {
        if (amount <= 0) throw new RuntimeException("Geçersiz tutar.");

        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Yetersiz bakiye.");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        return walletRepository.save(wallet);
    }
    public Double getBalance(Long userId) {
        return getWalletByUserId(userId).getBalance();
    }

}
