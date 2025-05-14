package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.MovementTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface MovementTimeRepository extends JpaRepository<MovementTime, Long> {

    List<MovementTime> findByRouteId(Long routeId);

    // Şu anki saate tam olarak eşit olan kayıtları getirir
    List<MovementTime> findByTime(LocalTime time);

    //native query ile sadece saat ve dakikası eşleşenleri getir
    @Query(value = """
        SELECT * 
        FROM movementtime
        WHERE EXTRACT(HOUR FROM time) = :hour
        AND EXTRACT(MINUTE FROM time) = :minute 
      """, nativeQuery = true)
    List<MovementTime> findByHourAndMinute(@Param("hour") int hour,@Param("minute") int minute);
}
