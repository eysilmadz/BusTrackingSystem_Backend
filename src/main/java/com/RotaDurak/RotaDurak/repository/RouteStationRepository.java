package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.Route;
import com.RotaDurak.RotaDurak.model.RouteStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RouteStationRepository extends JpaRepository<RouteStation, Long>{
    List<RouteStation> findByRoute(Route route); // Belirli bir rotaya ait istasyonları getir
}
