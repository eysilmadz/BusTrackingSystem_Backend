package com.RotaDurak.RotaDurak.model;
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
    private Long id;

    @Column(name = "cardType", nullable = false, length = 50)
    private String cardtype;

    @Column(name = "cardNumber", nullable = false, unique = true, length = 16)
    private String cardnumber;

    @Column(name = "expirydate", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "cardprovider", nullable = false, length = 50)
    private String cardProvider;

    @Column(name = "createdat", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @OneToMany(mappedBy = "bankCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankTransaction> transactions;
}
