package com.RotaDurak.RotaDurak.controller;

import com.RotaDurak.RotaDurak.model.User;
import com.RotaDurak.RotaDurak.model.Wallet;
import com.RotaDurak.RotaDurak.repository.UserRepository;
import com.RotaDurak.RotaDurak.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/balance/{userId}")
    public ResponseEntity<Double> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getBalance(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Wallet> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @PostMapping("/create")
    public ResponseEntity<Wallet> createWallet(@RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
        return ResponseEntity.ok(walletService.createWallet(user));
    }
}
