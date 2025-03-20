package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Route findByName(String name); // Rota ismine göre arama
}
