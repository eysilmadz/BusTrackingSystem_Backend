package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.dto.PointDto;
import com.RotaDurak.RotaDurak.model.Direction;
import com.RotaDurak.RotaDurak.model.Route;
import com.RotaDurak.RotaDurak.model.RouteStation;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
@Repository
public interface RouteStationRepository extends JpaRepository<RouteStation, Long>{
    List<RouteStation> findByRouteId(Long routeId); // Belirli bir rotaya ait istasyonları getir

    List<RouteStation> findByStationId(Long stationId);

    @Query("""
      select s.location 
      from RouteStation rs 
      join rs.station s 
      where rs.route.id = :routeId
    """)
    List<String> findStationLocationsByRouteId(@Param("routeId") Long routeId);

    Long route(Route route);

    @Query("SELECT rs FROM RouteStation rs WHERE rs.route.city = :cityId")
    List<RouteStation> findByRoute_CityId(@Param("cityId") Long cityId);

    List<RouteStation> findByRoute_City_Id(Long cityId);

}
