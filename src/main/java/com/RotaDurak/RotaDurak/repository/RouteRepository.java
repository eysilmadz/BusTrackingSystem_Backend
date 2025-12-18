package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.dto.PointDto;
import com.RotaDurak.RotaDurak.model.Direction;
import com.RotaDurak.RotaDurak.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByCity_Id(Long cityId); //Şehre ait hatları getirir

    Optional<Route> findByLineAndName(String line, String name);

    List<Route> findByCityId(Long cityId);
}
