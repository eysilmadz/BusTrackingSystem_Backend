package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    Station findByName(String name); // İstasyon ismine göre arama

    Optional<Station> findByNameAndLocation(String name, String location);
}
