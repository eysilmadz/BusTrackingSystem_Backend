package com.RotaDurak.RotaDurak.planner;

import com.RotaDurak.RotaDurak.dto.SegmentDto;
import com.RotaDurak.RotaDurak.planner.CostType;
import com.RotaDurak.RotaDurak.planner.CostWeights;
import com.RotaDurak.RotaDurak.planner.Graph;
import com.RotaDurak.RotaDurak.model.RouteStation;
import com.RotaDurak.RotaDurak.model.ShapePoint;
import com.RotaDurak.RotaDurak.model.Station;
import com.RotaDurak.RotaDurak.repository.RouteRepository;
import com.RotaDurak.RotaDurak.repository.RouteStationRepository;
import com.RotaDurak.RotaDurak.repository.ShapePointRepository;
import com.RotaDurak.RotaDurak.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

//Rota planlama iş mantığı
@Service
public class PlannerService {
    @Autowired private RouteStationRepository routeStationRepository;
    @Autowired private StationRepository stationRepository;
    @Autowired private ShapePointRepository shapePointRepository;
    @Autowired private RouteRepository routeRepository;

    //sadece stationId dizisini döner
    public List<Long> planRoute(double fromLat, double fromLon, double toLat, double toLon, CostType costType) {
        Graph graph = new Graph();
        List<Station> allStations = stationRepository.findAll();
        long startId = findClosestStationId(fromLat, fromLon, allStations);
        long goalId  = findClosestStationId(toLat,   toLon,   allStations);
        return graph.shortestPath(startId, goalId);
    }

    //Her segmenti mode/duration/distance ile döner
    public List<SegmentDto> planRouteSegments(double fromLat, double fromLon,
                                              double toLat,   double toLon,
                                              CostType costType) {
        Graph graph = buildGraph(costType);
        List<Station> allStations = stationRepository.findAll();
        long startId = findClosestStationId(fromLat, fromLon, allStations);
        long goalId  = findClosestStationId(toLat,   toLon,   allStations);
        List<Long> path = graph.shortestPath(startId, goalId);

        // SegmentDto’ları oluştur
        List<SegmentDto> segments = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            long a = path.get(i), b = path.get(i+1);
            // a → b kenarını al
            Graph.Edge edge = graph.getNeighbors(a).stream()
                    .filter(e -> e.toStationId == b)
                    .findFirst()
                    .orElseThrow();

            // Gerçek mesafe (km)
            String locA = stationRepository.findById(a).get().getLocation(); // "lat,lon"
            String locB = stationRepository.findById(b).get().getLocation();
            String[] partsA = locA.split(",");
            String[] partsB = locB.split(",");
            double latA = Double.parseDouble(partsA[0]);
            double lonA = Double.parseDouble(partsA[1]);
            double latB = Double.parseDouble(partsB[0]);
            double lonB = Double.parseDouble(partsB[1]);

            double realDistKm = Graph.haversineKm(latA, lonA, latB, lonB);
            double durationMin = edge.weight * 60;
            double distanceKm = realDistKm;

            SegmentDto seg = new SegmentDto();
            seg.setFromStationId(a);
            seg.setToStationId(b);
            seg.setDurationMin(durationMin);
            seg.setDistanceKm(distanceKm);

            if (edge.routeId == null) {
                seg.setMode("WALK");
                seg.setRouteLine(null);
            } else {
                seg.setMode("BUS");
                String line = routeRepository.findById(edge.routeId)
                        .map(r -> r.getLine())
                        .orElse("?");
                seg.setRouteLine(line);
            }
            segments.add(seg);
        }
        List<SegmentDto> merged = new ArrayList<>();
        for (SegmentDto seg : segments) {
            if (!merged.isEmpty()) {
                SegmentDto prev = merged.get(merged.size() - 1);
                if ("BUS".equals(seg.getMode())
                        && "BUS".equals(prev.getMode())
                        && Objects.equals(seg.getRouteLine(), prev.getRouteLine())) {
                    prev.setDurationMin(prev.getDurationMin() + seg.getDurationMin());
                    prev.setDistanceKm(prev.getDistanceKm() + seg.getDistanceKm());
                    continue;
                }
            }
            merged.add(seg);
        }

        return merged;
    }


    // --- Graph kurulumunu soyutladık ---
    private Graph buildGraph(CostType costType) {
        Graph graph = new Graph();
        // 1) Otobüs kenarları
        List<RouteStation> allRS = routeStationRepository.findAll();
        Map<Long, List<RouteStation>> byRoute = allRS.stream()
                .collect(Collectors.groupingBy(rs -> rs.getRoute().getId()));

        for (var entry : byRoute.entrySet()) {
            Long routeId = entry.getKey();
            List<RouteStation> stationsOnRoute = entry.getValue();
            String shapeId = stationsOnRoute.get(0).getRoute().getShape();
            List<ShapePoint> points = shapePointRepository
                    .findByShapeidOrderBySequenceAsc(shapeId);

            Map<String, Integer> seqIndex = new HashMap<>();
            for (int i = 0; i < points.size(); i++) {
                ShapePoint p = points.get(i);
                seqIndex.put(p.getLat() + "," + p.getLon(), i);
            }

            stationsOnRoute.sort(Comparator.comparingInt(rs ->
                    seqIndex.getOrDefault(rs.getStation().getLocation(), Integer.MAX_VALUE)
            ));

            for (int i = 0; i < stationsOnRoute.size() - 1; i++) {
                Station s1 = stationsOnRoute.get(i).getStation();
                Station s2 = stationsOnRoute.get(i+1).getStation();
                double distKm = Graph.haversineKm(
                        parseLat(s1.getLocation()), parseLon(s1.getLocation()),
                        parseLat(s2.getLocation()), parseLon(s2.getLocation())
                );
                double w = CostWeights.computeWeight(distKm, costType);
                graph.addEdge(s1.getId(), s2.getId(), w, routeId);
            }
        }

        // 2) Yürüme kenarları (0.5 km eşiği)
        double walkingThresholdKm = 0.5;
        List<Station> allStations = stationRepository.findAll();
        for (int i = 0; i < allStations.size(); i++) {
            Station s1 = allStations.get(i);
            for (int j = i+1; j < allStations.size(); j++) {
                Station s2 = allStations.get(j);
                double distKm = Graph.haversineKm(
                        parseLat(s1.getLocation()), parseLon(s1.getLocation()),
                        parseLat(s2.getLocation()), parseLon(s2.getLocation())
                );
                if (distKm <= walkingThresholdKm) {
                    double w = CostWeights.computeWeight(distKm, CostType.WALK);
                    graph.addEdge(s1.getId(), s2.getId(), w, null);
                }
            }
        }
        return graph;
    }

    private double parseLat(String loc)  { return Double.parseDouble(loc.split(",")[0]); }
    private double parseLon(String loc)  { return Double.parseDouble(loc.split(",")[1]); }

    private long findClosestStationId(double lat, double lon, List<Station> stations) {
        double minDist = Double.MAX_VALUE;
        long bestId   = -1;
        for (Station s : stations) {
            double d = Graph.haversineKm(
                    parseLat(s.getLocation()), parseLon(s.getLocation()),
                    lat, lon
            );
            if (d < minDist) {
                minDist = d;
                bestId   = s.getId();
            }
        }
        return bestId;
    }
}
