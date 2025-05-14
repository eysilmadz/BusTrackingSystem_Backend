package com.RotaDurak.RotaDurak.service;

import com.RotaDurak.RotaDurak.dto.PointDto;
import com.RotaDurak.RotaDurak.repository.ShapePointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShapeCacheService {
    @Autowired
    private ShapePointRepository shapePointRepository;

    public List<PointDto> getShape(String shapeid) {
        return shapePointRepository
                .findByShapeidOrderBySequenceAsc(shapeid)
                .stream()
                .map(p -> new PointDto(p.getLat(), p.getLon()))
                .collect(Collectors.toList());
    }
}
