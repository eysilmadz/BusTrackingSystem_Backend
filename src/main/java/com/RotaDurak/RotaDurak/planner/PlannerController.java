package com.RotaDurak.RotaDurak.planner;

import com.RotaDurak.RotaDurak.dto.SegmentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planner")
@CrossOrigin(origins = "*")
public class PlannerController {
    private static final Logger logger = LoggerFactory.getLogger(PlannerController.class);

    @Autowired
    private PlannerService plannerService;

    @GetMapping
    public ResponseEntity<?> plan(
            @RequestParam double fromLat,
            @RequestParam double fromLon,
            @RequestParam double toLat,
            @RequestParam double toLon,
            @RequestParam CostType type,
            @RequestParam Long cityId
    ) {
        try {
            // Parametre validasyonu
            if (!isValidCoordinate(fromLat, fromLon) || !isValidCoordinate(toLat, toLon)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid coordinates"));
            }

            if (cityId == null || cityId <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid city ID"));
            }

            logger.info("Planning route: from=({},{}) to=({},{}) type={} cityId={}",
                    fromLat, fromLon, toLat, toLon, type, cityId);

            List<Long> result = plannerService.planRoute(fromLat, fromLon, toLat, toLon, type, cityId);

            if (result.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "message", "No route found",
                        "path", Collections.emptyList()
                ));
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error in plan endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/segments")
    public ResponseEntity<?> planSegments(
            @RequestParam double fromLat,
            @RequestParam double fromLon,
            @RequestParam double toLat,
            @RequestParam double toLon,
            @RequestParam CostType type,
            @RequestParam Long cityId
    ) {
        try {
            // Parametre validasyonu
            if (!isValidCoordinate(fromLat, fromLon) || !isValidCoordinate(toLat, toLon)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid coordinates"));
            }

            if (cityId == null || cityId <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid city ID"));
            }

            logger.info("Planning route segments: from=({},{}) to=({},{}) type={} cityId={}",
                    fromLat, fromLon, toLat, toLon, type, cityId);

            List<SegmentDto> result = plannerService.planRouteSegments(fromLat, fromLon, toLat, toLon, type, cityId);

            if (result.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "message", "No route found",
                        "segments", Collections.emptyList(),
                        "totalDuration", 0,
                        "totalDistance", 0
                ));
            }

            // Toplam süre ve mesafe hesapla
            double totalDuration = result.stream().mapToDouble(SegmentDto::getDurationMin).sum();
            double totalDistance = result.stream().mapToDouble(SegmentDto::getDistanceKm).sum();

            Map<String, Object> response = Map.of(
                    "segments", result,
                    "totalDuration", Math.round(totalDuration * 100.0) / 100.0,
                    "totalDistance", Math.round(totalDistance * 100.0) / 100.0,
                    "segmentCount", result.size()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in planSegments endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/clear-cache")
    public ResponseEntity<?> clearCache(@RequestParam(required = false) Long cityId) {
        try {
            if (cityId != null) {
                plannerService.clearCacheForCity(cityId);
                return ResponseEntity.ok(Map.of("message", "Cache cleared for city: " + cityId));
            } else {
                plannerService.clearCache();
                return ResponseEntity.ok(Map.of("message", "All caches cleared"));
            }
        } catch (Exception e) {
            logger.error("Error clearing cache", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error clearing cache"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "timestamp", Instant.now().toString()));
    }

    private boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }
}
