package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.PopularPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopularPlaceRepository extends JpaRepository<PopularPlace, Long> {
    List<PopularPlace> findByCity_Id(Long cityId);
}
