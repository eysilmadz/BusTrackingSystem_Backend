package com.RotaDurak.RotaDurak.controller;

import com.RotaDurak.RotaDurak.dto.BusPaymentRequest;
import com.RotaDurak.RotaDurak.dto.CheckoutFormRequest;
import com.RotaDurak.RotaDurak.dto.LoadBalanceRequest;
import com.RotaDurak.RotaDurak.dto.PaymentResult;
import com.RotaDurak.RotaDurak.model.BankCard;
import com.RotaDurak.RotaDurak.model.BankTransaction;
import com.RotaDurak.RotaDurak.model.User;
import com.RotaDurak.RotaDurak.repository.BankTransactionRepository;
import com.RotaDurak.RotaDurak.repository.UserRepository;
import com.RotaDurak.RotaDurak.service.BankCardService;
import com.RotaDurak.RotaDurak.service.IyzicoPaymentService;
import com.RotaDurak.RotaDurak.service.WalletService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzipay.model.CheckoutForm;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired
    private IyzicoPaymentService iyzicoPaymentService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private BankTransactionRepository transactionRepository;

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private UserRepository userRepository;

    // İYZİCO ile cüzdana para yükle
    @PostMapping("/load-balance")
    public ResponseEntity<PaymentResult> loadBalance(@RequestBody LoadBalanceRequest request) {
        return ResponseEntity.ok(iyzicoPaymentService.loadBalanceWithCard(request));
    }

    // NFC veya QR ile otobüs ödemesi
    @PostMapping("/bus-payment")
    public ResponseEntity<PaymentResult> busPayment(@RequestBody BusPaymentRequest request) {
        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

            if ("NFC".equals(request.getPaymentMethod()) && request.getToken() != null) {
                // NFC → sanal kart bakiyesinden düş
                BankCard card = bankCardService.getCardByNfcToken(request.getToken());

                if (!card.getUser().getId().equals(request.getUserId())) {
                    return ResponseEntity.badRequest()
                            .body(new PaymentResult(false, "Bu kart size ait değil.", null));
                }

                // amount null gelirse default 15.0 kullan
                Double amount = request.getAmount() != null ? request.getAmount() : 3.0;

                if (card.getBalance() < amount) {
                    return ResponseEntity.badRequest()
                            .body(new PaymentResult(false, "Yetersiz kart bakiyesi. Lütfen sanal kartınıza para yükleyin.", null));
                }

                System.out.println(">>> NFC ödeme - cardId: " + card.getId() + " amount: " + request.getAmount());
                bankCardService.deductBalanceFromCard(card.getId(), amount);
                System.out.println(">>> Bakiye düşüldü - yeni bakiye: " + card.getBalance());

                BankTransaction tx = new BankTransaction();
                tx.setUser(user);
                tx.setAmount(amount);
                tx.setTransactionType("BUS_PAYMENT");
                tx.setStatus("SUCCESS");
                tx.setDescription("NFC otobüs ödemesi - " + (request.getBusLine() != null ? request.getBusLine() : "Rota Durak"));
                tx.setTransactionDate(LocalDateTime.now());
                transactionRepository.save(tx);

                return ResponseEntity.ok(new PaymentResult(true, "Ödeme başarılı.", null));

            }
            // QR ise token'ı parse et ve userId doğrula
            else if ("QR".equals(request.getPaymentMethod()) && request.getToken() != null) {
                // QR → cüzdan bakiyesinden düş
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode qr = mapper.readTree(request.getToken());
                    Long qrUserId = qr.get("userId").asLong();
                    if (!qrUserId.equals(request.getUserId())) {
                        return ResponseEntity.badRequest()
                                .body(new PaymentResult(false, "QR kod geçersiz.", null));
                    }

                    walletService.deductBalance(request.getUserId(), request.getAmount());

                    BankTransaction tx = new BankTransaction();
                    tx.setUser(user); // ← null değil artık
                    tx.setAmount(request.getAmount());
                    tx.setTransactionType("BUS_PAYMENT");
                    tx.setStatus("SUCCESS");
                    tx.setDescription("Otobüs ödemesi - " + request.getBusLine());
                    tx.setTransactionDate(LocalDateTime.now());
                    transactionRepository.save(tx);

                    return ResponseEntity.ok(new PaymentResult(true, "Ödeme başarılı.", null));
                } catch (Exception e) {
                    return ResponseEntity.badRequest()
                            .body(new PaymentResult(false, "QR kod okunamadı.", null));
                }
            }else {
                return ResponseEntity.badRequest()
                        .body(new PaymentResult(false, "Geçersiz ödeme yöntemi.", null));

            }

            } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new PaymentResult(false, e.getMessage(), null));
        }
    }

    // Kullanıcının kayıtlı banka kartlarını listele (ödeme sırasında seçim için)
    @GetMapping("/cards/{userId}")
    public ResponseEntity<List<BankCard>> getUserBankCards(@PathVariable Long userId) {
        return ResponseEntity.ok(bankCardService.getCardsByUserId(userId));
    }

    @PostMapping("/checkout-form")
    public ResponseEntity<?> initCheckoutForm(@RequestBody CheckoutFormRequest request) {
        return ResponseEntity.ok(iyzicoPaymentService.initializeCheckoutForm(request));
    }

    @PostMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestParam String token) {
        System.out.println(">>> callback geldi, token: " + token);
        iyzicoPaymentService.verifyAndLoadBalance(token);
        return ResponseEntity.ok("OK"); // İYZİCO'ya her zaman 200 dön
    }
}
