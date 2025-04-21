package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.CardReloadPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardReloadPointRepository extends JpaRepository<CardReloadPoint, Long> {
    List<CardReloadPoint> findByCity_Id(Long cityId);
}
