package com.RotaDurak.RotaDurak.service;

import com.RotaDurak.RotaDurak.dto.CheckoutFormRequest;
import com.RotaDurak.RotaDurak.dto.LoadBalanceRequest;
import com.RotaDurak.RotaDurak.dto.PaymentResult;
import com.RotaDurak.RotaDurak.model.BankCard;
import com.RotaDurak.RotaDurak.model.BankTransaction;
import com.RotaDurak.RotaDurak.model.User;
import com.RotaDurak.RotaDurak.repository.BankCardRepository;
import com.RotaDurak.RotaDurak.repository.BankTransactionRepository;
import com.RotaDurak.RotaDurak.repository.UserRepository;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.iyzipay.request.CreatePaymentRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@Transactional
public class IyzicoPaymentService {

    @Autowired
    private Options iyzicoOptions;

    @Autowired
    private WalletService walletService;

    @Autowired
    private BankTransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private BankCardService bankCardService;

    @Value("${iyzico.callbackUrl}")
    private String callbackUrl;

    // React Native'den gelen para yükleme isteği
    public PaymentResult loadBalanceWithCard(LoadBalanceRequest request) {
        System.out.println(">>> userId: " + request.getUserId());
        System.out.println(">>> amount: " + request.getAmount());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        // İYZİCO options'ı logla
        System.out.println(">>> apiKey: " + iyzicoOptions.getApiKey());
        System.out.println(">>> baseUrl: " + iyzicoOptions.getBaseUrl());


        // İYZİCO ödeme nesnesi oluştur
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setLocale(Locale.TR.getValue());
        paymentRequest.setConversationId("wallet-load-" + user.getId() + "-" + System.currentTimeMillis());
        paymentRequest.setPrice(BigDecimal.valueOf(request.getAmount()));
        paymentRequest.setPaidPrice(BigDecimal.valueOf(request.getAmount()));
        paymentRequest.setCurrency(Currency.TRY.name());
        paymentRequest.setInstallment(1);
        paymentRequest.setPaymentChannel(PaymentChannel.MOBILE.name());
        paymentRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        // Kart bilgisi
        PaymentCard paymentCard = new PaymentCard();
        if (request.getCardToken() != null) {
            // Kayıtlı kart ile ödeme
            paymentCard.setCardToken(request.getCardToken());
            paymentCard.setCardUserKey(request.getCardUserKey());
            paymentCard.setCvc(request.getCvc());
            paymentCard.setRegisterCard(0);
        } else {
            // Manuel kart girişi
            paymentCard.setCardHolderName(request.getCardHolderName());
            paymentCard.setCardNumber(request.getCardNumber());
            paymentCard.setExpireMonth(request.getExpireMonth());
            paymentCard.setExpireYear(request.getExpireYear());
            paymentCard.setCvc(request.getCvc());
            if (request.isSaveCard()) {
                paymentCard.setRegisterCard(1);
                if (request.getCardUserKey() != null) {
                    paymentCard.setCardUserKey(request.getCardUserKey());
                }
            } else {
                paymentCard.setRegisterCard(0);
            }
        }
        paymentRequest.setPaymentCard(paymentCard);

        // Alıcı bilgileri
        Buyer buyer = new Buyer();
        buyer.setId(user.getId().toString());
        buyer.setName(user.getFirstName());
        buyer.setSurname(user.getLastName());
        buyer.setEmail(user.getEmail());
        buyer.setGsmNumber(user.getPhoneNumber());
        buyer.setIdentityNumber("11111111111"); // Gerçekte TC kimlik olmalı
        buyer.setRegistrationAddress("Türkiye");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        paymentRequest.setBuyer(buyer);

        // Adres
        Address address = new Address();
        address.setContactName(user.getFirstName() + " " + user.getLastName());
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddress("Türkiye");
        paymentRequest.setShippingAddress(address);
        paymentRequest.setBillingAddress(address);

        // Sepet (zorunlu alan)
        BasketItem item = new BasketItem();
        item.setId("wallet-topup");
        item.setName("Cüzdan Para Yükleme");
        item.setCategory1("Ulaşım");
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(BigDecimal.valueOf(request.getAmount()));
        paymentRequest.setBasketItems(List.of(item));

        // İYZİCO'ya gönder
        Payment result = Payment.create(paymentRequest, iyzicoOptions);

        // İYZİCO'dan dönen cevabı logla
        System.out.println(">>> iyzico status: " + result.getStatus());
        System.out.println(">>> iyzico errorCode: " + result.getErrorCode());
        System.out.println(">>> iyzico errorMessage: " + result.getErrorMessage());
        System.out.println(">>> iyzico errorGroup: " + result.getErrorGroup());

        // Transaction kaydı oluştur
        BankTransaction transaction = new BankTransaction();
        transaction.setUser(user);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType("LOAD_BALANCE");
        transaction.setIyzicoReferenceCode(result.getPaymentId());
        transaction.setDescription("Cüzdana para yükleme - İYZİCO");
        transaction.setTransactionDate(LocalDateTime.now());

        if ("success".equals(result.getStatus())) {
            transaction.setStatus("SUCCESS");

            System.out.println(">>> targetType: " + request.getTargetType());
            System.out.println(">>> cardId: " + request.getCardId());

            // Nereye yüklenecek?
            if ("VIRTUAL_CARD".equals(request.getTargetType()) && request.getCardId() != null) {
                bankCardService.loadBalanceToCard(request.getCardId(), request.getAmount());
                transaction.setDescription("Sanal karta para yükleme - İYZİCO");
            } else {
                walletService.loadBalance(request.getUserId(), request.getAmount());
                transaction.setDescription("Cüzdana para yükleme - İYZİCO");
            }

            transactionRepository.save(transaction);

            // Kart kaydedildiyse BankCard tablosuna token'ı kaydet
            if (request.isSaveCard() && result.getCardToken() != null) {
                String lastFour = request.getCardNumber().replace(" ", "");
                lastFour = lastFour.substring(lastFour.length() - 4);
                String masked = "**** **** **** " + lastFour;

                BankCard card = new BankCard();
                card.setUser(user);
                card.setCardToken(result.getCardToken());
                card.setCardUserKey(result.getCardUserKey());
                card.setCardNumber(masked);
                card.setMaskedNumber(masked);
                card.setCardAlias(request.getCardHolderName());
                card.setCardProvider(result.getCardFamily() != null ? result.getCardFamily() : "CARD");
                card.setCardType("CREDIT");
                card.setIsActive(true);

                bankCardRepository.save(card);
                System.out.println(">>> Kart kaydedildi: " + masked);
            }

            return new PaymentResult(true, "Para yükleme başarılı.", result.getPaymentId());
        } else {
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
            return new PaymentResult(false, result.getErrorMessage(), null);
        }
    }

    //Checkout Form (WebView ile ödeme)
    public Map<String, String> initializeCheckoutForm(CheckoutFormRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        CreateCheckoutFormInitializeRequest checkoutRequest =
                new CreateCheckoutFormInitializeRequest();

        checkoutRequest.setLocale(Locale.TR.getValue());
        checkoutRequest.setConversationId("wallet-" + user.getId() + "-" + System.currentTimeMillis());
        checkoutRequest.setPrice(BigDecimal.valueOf(request.getAmount()));
        checkoutRequest.setPaidPrice(BigDecimal.valueOf(request.getAmount()));
        checkoutRequest.setCurrency(Currency.TRY.name());
        checkoutRequest.setBasketId("wallet-topup-" + user.getId());
        checkoutRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        // Callback URL — ödeme sonrası buraya döner
        checkoutRequest.setCallbackUrl(callbackUrl);
        System.out.println(">>> callbackUrl: " + callbackUrl);

        Buyer buyer = new Buyer();
        buyer.setId(user.getId().toString());
        buyer.setName(user.getFirstName());
        buyer.setSurname(user.getLastName());
        buyer.setEmail(user.getEmail());
        buyer.setGsmNumber(user.getPhoneNumber());
        buyer.setIdentityNumber("11111111111");
        buyer.setRegistrationAddress("Türkiye");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        checkoutRequest.setBuyer(buyer);

        Address address = new Address();
        address.setContactName(user.getFirstName() + " " + user.getLastName());
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddress("Türkiye");
        checkoutRequest.setShippingAddress(address);
        checkoutRequest.setBillingAddress(address);

        BasketItem item = new BasketItem();
        item.setId("wallet-topup");
        item.setName("Cüzdan Para Yükleme");
        item.setCategory1("Ulaşım");
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(BigDecimal.valueOf(request.getAmount()));
        checkoutRequest.setBasketItems(List.of(item));

        CheckoutFormInitialize checkoutForm =
                CheckoutFormInitialize.create(checkoutRequest, iyzicoOptions);

        System.out.println(">>> checkout status: " + checkoutForm.getStatus());
        System.out.println(">>> checkout token: " + checkoutForm.getToken());
        System.out.println(">>> checkout paymentPageUrl: " + checkoutForm.getPaymentPageUrl());
        System.out.println(">>> checkoutFormContent length: " +
                (checkoutForm.getCheckoutFormContent() != null ?
                        checkoutForm.getCheckoutFormContent().length() : "NULL"));

        Map<String, String> result = new HashMap<>();
        result.put("checkoutFormContent", checkoutForm.getCheckoutFormContent());
        result.put("token", checkoutForm.getToken());
        result.put("status", checkoutForm.getStatus());
        result.put("paymentPageUrl", checkoutForm.getPaymentPageUrl());
        return result;
    }

    // ─── Checkout Form callback doğrulama ───────────────────────────────────────
    public boolean verifyAndLoadBalance(String token) {
        try {
            RetrieveCheckoutFormRequest retrieveRequest = new RetrieveCheckoutFormRequest();
            retrieveRequest.setToken(token);
            retrieveRequest.setLocale(Locale.TR.getValue());
            retrieveRequest.setConversationId(token);

            CheckoutForm checkoutForm = CheckoutForm.retrieve(retrieveRequest, iyzicoOptions);

            System.out.println(">>> verify status: " + checkoutForm.getPaymentStatus());
            System.out.println(">>> verify conversationId: " + checkoutForm.getConversationId());
            System.out.println(">>> verify basketId: " + checkoutForm.getBasketId());

            if ("SUCCESS".equals(checkoutForm.getPaymentStatus())) {
                // conversationId = "userId:amount"
                //String[] parts = checkoutForm.getConversationId().split(":");
                //Long userId = Long.parseLong(parts[0]);
                //Double amount = Double.parseDouble(parts[1]);
                String conversationId = checkoutForm.getConversationId();
                String basketId = checkoutForm.getBasketId(); // "wallet-topup-19"

                System.out.println(">>> basketId: " + basketId);

                Long userId;
                Double amount;

                if (conversationId != null && conversationId.contains(":")) {
                    // conversationId = "19:10.0"
                    String[] parts = conversationId.split(":");
                    userId = Long.parseLong(parts[0]);
                    amount = Double.parseDouble(parts[1]);
                } else if (basketId != null && basketId.startsWith("wallet-topup-")) {
                    // Fallback: basketId'den userId al, amount için price'a bak
                    userId = Long.parseLong(basketId.replace("wallet-topup-", ""));
                    amount = checkoutForm.getPrice().doubleValue();
                } else {
                    System.out.println(">>> userId ve amount belirlenemedi!");
                    return false;
                }

                System.out.println(">>> userId: " + userId + " amount: " + amount);


                // Duplicate önleme
                boolean alreadyProcessed = transactionRepository
                        .existsByIyzicoReferenceCode(token);
                if (alreadyProcessed) {
                    System.out.println(">>> Bu token zaten işlendi: " + token);
                    return true;
                }

                User user = userRepository.findById(userId).orElseThrow();

                BankTransaction tx = new BankTransaction();
                tx.setUser(user);
                tx.setAmount(amount);
                tx.setTransactionType("LOAD_BALANCE");
                tx.setStatus("SUCCESS");
                tx.setIyzicoReferenceCode(token);
                tx.setDescription("İYZİCO Checkout ile bakiye yükleme");
                tx.setTransactionDate(LocalDateTime.now());
                transactionRepository.save(tx);

                walletService.loadBalance(userId, amount);

                System.out.println(">>> Bakiye güncellendi! userId: " + userId + " amount: " + amount);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println(">>> verify hatası: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
