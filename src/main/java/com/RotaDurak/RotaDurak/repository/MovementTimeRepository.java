package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.MovementTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovementTimeRepository extends JpaRepository<MovementTime, Long> {

    List<MovementTime> findByRouteId(Long routeId);

}
