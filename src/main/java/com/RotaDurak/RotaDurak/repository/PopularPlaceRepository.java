package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.PopularPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PopularPlaceRepository extends JpaRepository<PopularPlace, Long> {
    PopularPlace findByName(String name); // Popüler yer ismine göre arama sonra değişecek
}
