package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
@Repository
public interface CityRepository extends JpaRepository<City, Long>{
    @Query("SELECT c.name FROM City c")
    List<String> findAllCityNames();
}
