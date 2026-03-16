package com.RotaDurak.RotaDurak.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bankcard",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cardNumber"})
        })
public class BankCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cardid")
    private Long id;

    @Column(name = "cardtype", nullable = false, length = 50)
    private String cardType;

    @Column(name = "cardnumber", nullable = true, unique = true, length = 19)
    private String cardNumber;

    @Column(name = "expirydate", nullable = true)
    private LocalDateTime expiryDate;

    @Column(name = "cardprovider", nullable = true, length = 50)
    private String cardProvider;

    @Column(name = "createdat", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "bankCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankTransaction> transactions;

    // Sanal kart alanları — cardType="VIRTUAL" ise bunlar dolu gelir
    @Column(name = "qrcode", unique = true, length = 255)
    private String qrCode; // UUID bazlı, ödeme sırasında taranır

    @Column(name = "nfctoken", unique = true, length = 255)
    private String nfcToken; // NFC okuyucu bu token'ı doğrular

    @Column(name = "isactive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "nickname", length = 50)
    private String nickname; // "Benim Kartım" gibi kullanıcı tanımlı isim

    @Column(name = "cardtoken", length = 255)
    private String cardToken;

    @Column(name = "carduserkey", length = 255)
    private String cardUserKey;

    @Column(name = "maskednumber", length = 50)
    private String maskedNumber;

    @Column(name = "cardalias", length = 100)
    private String cardAlias;

    @Column(name = "balance", nullable = false)
    private Double balance = 0.0;
}
