package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByCity_Id(Long cityId); //Şehre ait hatları getirir
}
