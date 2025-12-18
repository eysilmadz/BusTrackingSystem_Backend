package com.RotaDurak.RotaDurak.planner;

import com.RotaDurak.RotaDurak.dto.SegmentDto;
import com.RotaDurak.RotaDurak.model.Route;
import com.RotaDurak.RotaDurak.model.RouteStation;
import com.RotaDurak.RotaDurak.model.ShapePoint;
import com.RotaDurak.RotaDurak.model.Station;
import com.RotaDurak.RotaDurak.repository.RouteRepository;
import com.RotaDurak.RotaDurak.repository.RouteStationRepository;
import com.RotaDurak.RotaDurak.repository.ShapePointRepository;
import com.RotaDurak.RotaDurak.repository.StationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

//Rota planlama iş mantığı
@Service
public class PlannerService {

    private static final Logger logger = LoggerFactory.getLogger(PlannerService.class);
    private static final double MAX_WALKING_DISTANCE_KM = 0.5;

    @Autowired private RouteStationRepository routeStationRepository;
    @Autowired private StationRepository stationRepository;
    @Autowired private ShapePointRepository shapePointRepository;
    @Autowired private RouteRepository routeRepository;

    // Cache yapıları
    private final Map<Long, List<Station>> stationCache = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Station>> stationMapCache = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Route>> routeMapCache = new ConcurrentHashMap<>();
    private final Map<String, Graph> graphCache = new ConcurrentHashMap<>();

    private List<Station> getStations(Long cityId) {
        return stationCache.computeIfAbsent(cityId, id -> {
            logger.debug("Loading stations for city: {}", id);
            return stationRepository.findByCityId(id);
        });
    }

    private Map<Long, Station> getStationMap(Long cityId) {
        return stationMapCache.computeIfAbsent(cityId, id ->
                getStations(id).stream().collect(Collectors.toMap(Station::getId, Function.identity()))
        );
    }

    private Map<Long, Route> getRouteMap(Long cityId) {
        return routeMapCache.computeIfAbsent(cityId, id -> {
            logger.debug("Loading routes for city: {}", id);
            return routeRepository.findByCityId(id).stream()
                    .collect(Collectors.toMap(Route::getId, Function.identity()));
        });
    }

    private String getRouteLine(Long cityId, Long routeId) {
        Route route = getRouteMap(cityId).get(routeId);
        return (route != null) ? route.getLine() : "?";
    }

    public List<Long> planRoute(double fromLat, double fromLon, double toLat, double toLon, CostType costType, Long cityId) {
        try {
            Graph graph = buildGraph(cityId, costType);
            List<Station> stations = getStations(cityId);

            if (stations.isEmpty()) {
                logger.warn("No stations found for city: {}", cityId);
                return Collections.emptyList();
            }

            long startId = findClosestStationId(fromLat, fromLon, stations);
            long goalId = findClosestStationId(toLat, toLon, stations);

            if (startId == -1 || goalId == -1) {
                logger.warn("Could not find valid start or goal station");
                return Collections.emptyList();
            }

            return graph.shortestPath(startId, goalId);
        } catch (Exception e) {
            logger.error("Error planning route", e);
            return Collections.emptyList();
        }
    }

    public List<SegmentDto> planRouteSegments(double fromLat, double fromLon, double toLat, double toLon, CostType costType, Long cityId) {
        try {
            Graph graph = buildGraph(cityId, costType);
            List<Station> stations = getStations(cityId);
            Map<Long, Station> stationMap = getStationMap(cityId);

            if (stations.isEmpty()) {
                logger.warn("No stations found for city: {}", cityId);
                return Collections.emptyList();
            }

            long startId = findClosestStationId(fromLat, fromLon, stations);
            long goalId = findClosestStationId(toLat, toLon, stations);

            if (startId == -1 || goalId == -1) {
                logger.warn("Could not find valid start or goal station");
                return Collections.emptyList();
            }

            List<Long> path = graph.shortestPath(startId, goalId);

            if (path.isEmpty()) {
                logger.warn("No path found from station {} to station {}", startId, goalId);
                return Collections.emptyList();
            }

            logger.info("Planning route from ({}, {}) to ({}, {}), Path: {}", fromLat, fromLon, toLat, toLon, path);

            List<SegmentDto> segments = createSegments(path, graph, stationMap, cityId);
            List<SegmentDto> merged = mergeSegments(segments);

            logger.info("Segment count: original={}, merged={}", segments.size(), merged.size());
            return merged;

        } catch (Exception e) {
            logger.error("Error planning route segments", e);
            return Collections.emptyList();
        }
    }

    private List<SegmentDto> createSegments(List<Long> path, Graph graph, Map<Long, Station> stationMap, Long cityId) {
        List<SegmentDto> segments = new ArrayList<>();

        for (int i = 0; i < path.size() - 1; i++) {
            long a = path.get(i);
            long b = path.get(i + 1);

            Graph.Edge edge = graph.getNeighbors(a).stream()
                    .filter(e -> e.toStationId == b)
                    .findFirst()
                    .orElse(null);

            if (edge == null) {
                logger.warn("No edge found between stations {} and {}", a, b);
                continue;
            }

            Station s1 = stationMap.get(a);
            Station s2 = stationMap.get(b);

            if (s1 == null || s2 == null) {
                logger.warn("Station not found: s1={}, s2={}", s1, s2);
                continue;
            }

            try {
                double dist = Graph.haversineKm(
                        parseLat(s1.getLocation()), parseLon(s1.getLocation()),
                        parseLat(s2.getLocation()), parseLon(s2.getLocation())
                );
                double duration = edge.weight * 60;

                SegmentDto dto = new SegmentDto();
                dto.setFromStationId(a);
                dto.setToStationId(b);
                dto.setDistanceKm(Math.round(dist * 100.0) / 100.0); // 2 decimal places
                dto.setDurationMin(Math.round(duration * 100.0) / 100.0); // 2 decimal places

                if (edge.routeId == null) {
                    dto.setMode("WALK");
                } else {
                    dto.setMode("BUS");
                    dto.setRouteLine(getRouteLine(cityId, edge.routeId));
                }
                segments.add(dto);
            } catch (Exception e) {
                logger.error("Error processing segment between stations {} and {}", a, b, e);
            }
        }

        return segments;
    }

    private List<SegmentDto> mergeSegments(List<SegmentDto> segments) {
        if (segments.isEmpty()) {
            return segments;
        }

        List<SegmentDto> merged = new ArrayList<>();

        for (SegmentDto seg : segments) {
            if (!merged.isEmpty()) {
                SegmentDto prev = merged.get(merged.size() - 1);

                // Aynı otobüs hattı birleştirme
                if ("BUS".equals(seg.getMode()) && "BUS".equals(prev.getMode())
                        && Objects.equals(seg.getRouteLine(), prev.getRouteLine())) {
                    prev.setDurationMin(prev.getDurationMin() + seg.getDurationMin());
                    prev.setDistanceKm(prev.getDistanceKm() + seg.getDistanceKm());
                    prev.setToStationId(seg.getToStationId());
                    continue;
                }

                // Yürüme segmentleri birleştirme
                if ("WALK".equals(seg.getMode()) && "WALK".equals(prev.getMode())) {
                    prev.setDurationMin(prev.getDurationMin() + seg.getDurationMin());
                    prev.setDistanceKm(prev.getDistanceKm() + seg.getDistanceKm());
                    prev.setToStationId(seg.getToStationId());
                    continue;
                }
            }
            merged.add(seg);
        }

        return merged;
    }

    private Graph buildGraph(Long cityId, CostType costType) {
        String cacheKey = cityId + "_" + costType.name();

        if (graphCache.containsKey(cacheKey)) {
            logger.debug("Returning cached graph for cityId {} and costType {}", cityId, costType);
            return graphCache.get(cacheKey);
        }

        long startTime = System.currentTimeMillis();
        logger.info("Building graph for cityId {} with costType {}", cityId, costType);

        Graph graph = new Graph();

        try {
            // Route connections ekle
            addRouteConnections(graph, cityId, costType);

            // Walking connections ekle
            addWalkingConnections(graph, cityId, costType);

            graphCache.put(cacheKey, graph);

            long buildTime = System.currentTimeMillis() - startTime;
            logger.info("Graph for cityId {} built in {} ms", cityId, buildTime);

        } catch (Exception e) {
            logger.error("Error building graph for cityId {}", cityId, e);
            throw new RuntimeException("Graph building failed", e);
        }

        return graph;
    }

    private void addRouteConnections(Graph graph, Long cityId, CostType costType) {
        List<RouteStation> allRS = routeStationRepository.findByRoute_City_Id(cityId);

        if (allRS.isEmpty()) {
            logger.warn("No route stations found for city: {}", cityId);
            return;
        }

        Map<Long, List<RouteStation>> byRoute = allRS.stream()
                .collect(Collectors.groupingBy(rs -> rs.getRoute().getId()));

        Map<String, List<ShapePoint>> shapeCache = new HashMap<>();

        for (var entry : byRoute.entrySet()) {
            Long routeId = entry.getKey();
            List<RouteStation> stationsOnRoute = entry.getValue();

            if (stationsOnRoute.isEmpty()) {
                continue;
            }

            try {
                String shapeId = stationsOnRoute.get(0).getRoute().getShape();
                if (shapeId == null || shapeId.trim().isEmpty()) {
                    logger.warn("No shape found for route: {}", routeId);
                    continue;
                }

                List<ShapePoint> points = shapeCache.computeIfAbsent(shapeId,
                        id -> shapePointRepository.findByShapeidOrderBySequenceAsc(id));

                if (points.isEmpty()) {
                    logger.warn("No shape points found for shape: {}", shapeId);
                    continue;
                }

                // Shape point index oluştur
                Map<String, Integer> seqIndex = new HashMap<>();
                for (int i = 0; i < points.size(); i++) {
                    ShapePoint p = points.get(i);
                    seqIndex.put(p.getLat() + "," + p.getLon(), i);
                }

                // Stations'ı shape sequence'e göre sırala
                stationsOnRoute.sort(Comparator.comparingInt(rs -> {
                    Station station = rs.getStation();
                    if (station == null || station.getLocation() == null) {
                        return Integer.MAX_VALUE;
                    }
                    return seqIndex.getOrDefault(station.getLocation(), Integer.MAX_VALUE);
                }));

                // Ardışık istasyonlar arası bağlantı ekle
                for (int i = 0; i < stationsOnRoute.size() - 1; i++) {
                    Station s1 = stationsOnRoute.get(i).getStation();
                    Station s2 = stationsOnRoute.get(i + 1).getStation();

                    if (s1 == null || s2 == null || s1.getLocation() == null || s2.getLocation() == null) {
                        continue;
                    }

                    double distKm = Graph.haversineKm(
                            parseLat(s1.getLocation()), parseLon(s1.getLocation()),
                            parseLat(s2.getLocation()), parseLon(s2.getLocation())
                    );

                    // **3 parametreli computeWeight çağrısı: yürüyüş değil (false)**
                    double weight = CostWeights.computeWeight(distKm, costType, false);
                    graph.addEdge(s1.getId(), s2.getId(), weight, routeId);
                    graph.addEdge(s2.getId(), s1.getId(), weight, routeId);
                }

            } catch (Exception e) {
                logger.error("Error processing route: {}", routeId, e);
            }
        }
    }

    private void addWalkingConnections(Graph graph, Long cityId, CostType costType) {
        List<Station> allStations = getStations(cityId);

        logger.debug("Adding walking connections for {} stations", allStations.size());

        // Büyük şehirler için optimize edilmiş yaklaşım
        if (allStations.size() > 1000) {
            addWalkingConnectionsOptimized(graph, allStations, costType);
        } else {
            addWalkingConnectionsBasic(graph, allStations, costType);
        }
    }

    private void addWalkingConnectionsBasic(Graph graph, List<Station> allStations, CostType costType) {
        for (int i = 0; i < allStations.size(); i++) {
            Station s1 = allStations.get(i);
            if (s1.getLocation() == null) continue;

            for (int j = i + 1; j < allStations.size(); j++) {
                Station s2 = allStations.get(j);
                if (s2.getLocation() == null) continue;

                try {
                    double distKm = Graph.haversineKm(
                            parseLat(s1.getLocation()), parseLon(s1.getLocation()),
                            parseLat(s2.getLocation()), parseLon(s2.getLocation())
                    );

                    if (distKm <= MAX_WALKING_DISTANCE_KM) {
                        // **3 parametreli computeWeight çağrısı: yürüyüş = true**
                        double weight = CostWeights.computeWeight(distKm, costType, true);
                        graph.addEdge(s1.getId(), s2.getId(), weight, null);
                        graph.addEdge(s2.getId(), s1.getId(), weight, null);
                    }
                } catch (Exception e) {
                    logger.debug("Error calculating distance between stations {} and {}", s1.getId(), s2.getId());
                }
            }
        }
    }

    private void addWalkingConnectionsOptimized(Graph graph, List<Station> allStations, CostType costType) {
        logger.info("Using optimized walking connections for {} stations", allStations.size());

        for (int i = 0; i < allStations.size(); i++) {
            Station s1 = allStations.get(i);
            if (s1.getLocation() == null) continue;

            double lat1 = parseLat(s1.getLocation());
            double lon1 = parseLon(s1.getLocation());

            for (int j = i + 1; j < allStations.size(); j++) {
                Station s2 = allStations.get(j);
                if (s2.getLocation() == null) continue;

                double lat2 = parseLat(s2.getLocation());
                double lon2 = parseLon(s2.getLocation());

                double latDiff = Math.abs(lat1 - lat2);
                double lonDiff = Math.abs(lon1 - lon2);

                if (latDiff > 0.01 || lonDiff > 0.01) {
                    continue;
                }

                try {
                    double distKm = Graph.haversineKm(lat1, lon1, lat2, lon2);

                    if (distKm <= MAX_WALKING_DISTANCE_KM) {
                        double weight = CostWeights.computeWeight(distKm, costType, true);
                        graph.addEdge(s1.getId(), s2.getId(), weight, null);
                        graph.addEdge(s2.getId(), s1.getId(), weight, null);
                    }
                } catch (Exception e) {
                    logger.debug("Error calculating distance between stations {} and {}", s1.getId(), s2.getId());
                }
            }
        }
    }

    private double parseLat(String loc) {
        try {
            if (loc == null || loc.trim().isEmpty()) {
                throw new IllegalArgumentException("Location is null or empty");
            }
            return Double.parseDouble(loc.split(",")[0].trim());
        } catch (Exception e) {
            logger.error("Error parsing latitude from location: {}", loc, e);
            throw new IllegalArgumentException("Invalid location format: " + loc);
        }
    }

    private double parseLon(String loc) {
        try {
            if (loc == null || loc.trim().isEmpty()) {
                throw new IllegalArgumentException("Location is null or empty");
            }
            String[] parts = loc.split(",");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Location must contain latitude and longitude");
            }
            return Double.parseDouble(parts[1].trim());
        } catch (Exception e) {
            logger.error("Error parsing longitude from location: {}", loc, e);
            throw new IllegalArgumentException("Invalid location format: " + loc);
        }
    }

    private long findClosestStationId(double lat, double lon, List<Station> stations) {
        double minDist = Double.MAX_VALUE;
        long bestId = -1;

        for (Station s : stations) {
            if (s.getLocation() == null) {
                continue;
            }

            try {
                double d = Graph.haversineKm(parseLat(s.getLocation()), parseLon(s.getLocation()), lat, lon);
                if (d < minDist) {
                    minDist = d;
                    bestId = s.getId();
                }
            } catch (Exception e) {
                logger.debug("Error calculating distance to station: {}", s.getId());
            }
        }

        if (bestId == -1) {
            logger.warn("No valid station found for coordinates: ({}, {})", lat, lon);
        } else {
            logger.debug("Closest station: {} at distance: {} km", bestId, minDist);
        }

        return bestId;
    }

    // Cache temizleme metodu (gerekirse)
    public void clearCache() {
        stationCache.clear();
        stationMapCache.clear();
        routeMapCache.clear();
        graphCache.clear();
        logger.info("All caches cleared");
    }

    // Belirli şehir için cache temizleme
    public void clearCacheForCity(Long cityId) {
        stationCache.remove(cityId);
        stationMapCache.remove(cityId);
        routeMapCache.remove(cityId);

        // Graph cache'den bu şehre ait tüm entry'leri temizle
        graphCache.entrySet().removeIf(entry -> entry.getKey().startsWith(cityId + "_"));

        logger.info("Cache cleared for city: {}", cityId);
    }
}
