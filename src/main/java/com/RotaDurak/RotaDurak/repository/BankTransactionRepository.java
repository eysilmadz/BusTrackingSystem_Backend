package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.lang.Long;
import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findByUserId(Long id); // Kullanıcının tüm işlemlerini getirir
    List<BankTransaction> findByBankCardId(Long bankCardId); // Belirli bir kartın işlemlerini getirir
}
