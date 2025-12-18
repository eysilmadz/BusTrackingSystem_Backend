package com.RotaDurak.RotaDurak.service;

import com.RotaDurak.RotaDurak.cache.ShapeCache;
import com.RotaDurak.RotaDurak.dto.PositionMessage;
import com.RotaDurak.RotaDurak.model.Direction;
import com.RotaDurak.RotaDurak.repository.RouteRepository;
import com.RotaDurak.RotaDurak.repository.RouteStationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.RotaDurak.RotaDurak.dto.PointDto;

import java.time.*;
import java.util.*;
import java.util.function.BiFunction;

@Slf4j
@Service
public class BusSimulationService {

    @Autowired private ShapeCache shapeCache;
    @Autowired private KafkaProducerService kafkaProducerService;
    @Autowired private RouteRepository routeRepository;
    @Autowired private RouteStationRepository routeStationRepository;

    @Async
    public void simulateRoute(Long routeId,
                              Direction direction,
                              int dwellSeconds,
                              double avgSpeedKmh)  {
        log.info("🚍 simulateRoute başlatıldı → routeId={}, direction={}, dwellSeconds={}s, speed={}km/h",
                routeId, direction, dwellSeconds, avgSpeedKmh);
        // 1) Shape noktaları
        String shapeId = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route bulunamadı: " + routeId))
                .getShape();

        List<PointDto> points = new ArrayList<>(shapeCache.getShape(shapeId));
        if (direction == Direction.endToStart) Collections.reverse(points);
        if (points.isEmpty()) {
            log.warn("‼️ Shape points empty for route {}", routeId);
            return;
        }
        // 2) Durak koordinatlarını çift olarak parse et
        List<PointDto> stationPoints = routeStationRepository
                .findStationLocationsByRouteId(routeId).stream()
                .map(locStr -> {
                    String[] parts = locStr.trim().split(",");
                    return new PointDto(
                            Double.parseDouble(parts[0]),
                            Double.parseDouble(parts[1])
                    );
                })
                .toList();
        log.debug("📌 Durak noktası sayısı: {}", stationPoints.size());

        //haversine fonksiyonu
        BiFunction<PointDto,PointDto,Double> distanceKm = (p1,p2) -> {
            final double R = 6371; //dünya yarıçapı (km)
            double dlat = Math.toRadians(p2.getLat()-p1.getLat());
            double dlon = Math.toRadians(p2.getLon()-p1.getLon());
            double a = Math.sin(dlat/2) * Math.sin(dlat/2)
                    + Math.cos(Math.toRadians(p1.getLat())) * Math.cos(Math.toRadians(p2.getLat()))
                    * Math.sin(dlon/2) * Math.sin(dlon/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return R * c;
        };

        // Eşik: 50 metre yakınsa durak
        final double stationThresholdKm = 0.05;

        // 3) Her noktayı gönder ve bir sonraki noktaya geçen süreyi hesapla
        for (int i = 0; i < points.size(); i++) {
            PointDto curr = points.get(i);

            // 4) Mesajı Kafka'ya publish et

            Integer speed    = Integer.parseInt("speed");
            kafkaProducerService.send(new PositionMessage(routeId, curr.getLat(), curr.getLon(), Instant.now(), speed,  "SIMULATOR", direction));
            log.debug("▶️ idx={} → lat={},lon={}, routeId={}", i, curr.getLat(), curr.getLon(), routeId);

            // 4b) Durakta mıyız? (yakınlık kontrolü)
            boolean isStation = stationPoints.stream()
                    .anyMatch(st -> distanceKm.apply(curr, st) <= stationThresholdKm);
            if (isStation) {
                log.info("⏸ Durakta (idx={}) → {}s bekleniyor., routeId={}", i, dwellSeconds,routeId);
                try {
                    Thread.sleep(dwellSeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("⚠️ Durak beklemesi kesildi.");
                    break;
                }
            }

            // 6) Bir sonraki noktaya giderken segment gecikmesi
            if (i < points.size() - 1) {
                PointDto next = points.get(i+1);
                double km = distanceKm.apply(curr, next);
                long travelMs = (long)((km / avgSpeedKmh) * 3_600_000);
                log.info("routeId={},⏱ Segment {}→{}: mesafe={}km, sleep={}ms",routeId, i, i+1, String.format("%.3f", km), travelMs);
                try { Thread.sleep(travelMs); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }

        log.info("✅ simulateRoute tamamlandı → routeId={}", routeId);
    }
}
