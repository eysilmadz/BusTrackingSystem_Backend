package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.MovementTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementTimeRepository extends JpaRepository<MovementTime, Long> {

}
