package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.CardReloadPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardReloadPointRepository extends JpaRepository<CardReloadPoint, Long> {
    CardReloadPoint findByName(String name); // Kart dolum noktası ismine göre arama
}
