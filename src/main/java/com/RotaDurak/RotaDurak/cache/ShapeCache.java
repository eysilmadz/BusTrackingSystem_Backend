package com.RotaDurak.RotaDurak.cache;

import com.RotaDurak.RotaDurak.model.ShapePoint;
import com.RotaDurak.RotaDurak.repository.ShapePointRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import com.RotaDurak.RotaDurak.dto.PointDto;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ShapeCache {

    @Autowired
    private ShapePointRepository shapePointRepository;

    private final Map<String, List<PointDto>> shapeCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadShapeCache() {
        List<ShapePoint> all = shapePointRepository
                .findAll(Sort.by("shapeid").and(Sort.by("sequence")));

        for(ShapePoint sp : all) {
            shapeCache
                    .computeIfAbsent(sp.getShapeid(), id -> new ArrayList<>())
                    .add(new PointDto(sp.getLat(), sp.getLon()));
        }

        shapeCache.replaceAll((id,list) -> Collections.unmodifiableList(list));
        System.out.println("Shape cache loaded"+shapeCache.size());
    }

    public List<PointDto> getShape(String shapeid) {
        return shapeCache.getOrDefault(shapeid,Collections.emptyList());
    }

    public Set<String> getAllShapeIds() {
        return Collections.unmodifiableSet(shapeCache.keySet());
    }
}
