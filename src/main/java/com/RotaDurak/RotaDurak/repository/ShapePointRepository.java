package com.RotaDurak.RotaDurak.repository;

import com.RotaDurak.RotaDurak.model.ShapePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShapePointRepository extends JpaRepository<ShapePoint, Long> {
    List<ShapePoint> findByShapeidOrderBySequenceAsc(String ShapeId);
}
