package com.RotaDurak.RotaDurak.controller;

import com.RotaDurak.RotaDurak.cache.ShapeCache;
import com.RotaDurak.RotaDurak.dto.PointDto;
import com.RotaDurak.RotaDurak.model.Direction;
import com.RotaDurak.RotaDurak.model.MovementTime;
import com.RotaDurak.RotaDurak.repository.MovementTimeRepository;
import com.RotaDurak.RotaDurak.repository.RouteRepository;
import com.RotaDurak.RotaDurak.service.BusSimulationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
@RequestMapping("/api/simulation")
public class SimulationController {
    @Autowired private BusSimulationService busSimulationService;
    @Autowired private ShapeCache shapeCache;
    @Autowired private RouteRepository routeRepository;
    @Autowired
    private MovementTimeRepository movementTimeRepository;

    @PostMapping("/{routeId}")
    public ResponseEntity<Void> simulate(
            @PathVariable Long routeId,
            @RequestBody SimRequest req
    ) {
       busSimulationService.simulateRoute(
               routeId,
               req.getDirection(),
               req.getDwellSeconds(),
               req.getAvgSpeedKmh()
       );
       return ResponseEntity.accepted().build();
    }


    //Rota şeklini döner
    @GetMapping("/{routeId}/path")
    public ResponseEntity<List<PointDto>> getPath(
            @PathVariable Long routeId,
            @RequestParam(name = "direction", defaultValue = "startToEnd") Direction direction) {
        //routeIdden shapeIdyi al
        // 1) Orijinal, unmodifiable listeyi al
        List<PointDto> original = shapeCache.getShape(
                routeRepository.findById(routeId)
                        .orElseThrow(() -> new IllegalArgumentException("Route bulunamadı: " + routeId))
                        .getShape()
        );

        // 2) Mutable kopyasını oluştur
        List<PointDto> path = new ArrayList<>(original);

        // 3) Eğer dönüş yönüyse listeyi ters çevir
        if (direction == Direction.endToStart) {
            Collections.reverse(path);
        }

        //JSON olarak geri dön
        return ResponseEntity.ok(path);
    }

    @PostMapping("/{routeId}/simulateAll")
    public ResponseEntity<Void> simulateAll(@PathVariable Long routeId,@RequestParam(defaultValue = "true") boolean futureOnly) {
        LocalTime now = LocalTime.now();
        List<MovementTime> list = movementTimeRepository.findByRouteId(routeId);
        if (futureOnly) {
            list = list.stream()
                    .filter(m -> !m.getTime().isBefore(now))
                    .toList();
        }
        list.forEach(m -> {
            try{
                busSimulationService.simulateRoute(
                routeId,
                m.getDirection(),
                10,
                40.0
            );
        }catch (Exception e) {
                log.error("❌ simulateRoute submission failed for MT#{}: {}", m.getId(), e.getMessage());
            }
        });
        return ResponseEntity.accepted().build();
    }

    @Data
    public static class SimRequest {
        private Direction direction; //gidiş-dönüş
        private int dwellSeconds =10; //her durakta bekleme suresi
        private double avgSpeedKmh = 40.0; //ortalama hız km/s
    }
}
