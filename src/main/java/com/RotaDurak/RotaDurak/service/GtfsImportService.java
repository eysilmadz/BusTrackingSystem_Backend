package com.RotaDurak.RotaDurak.service;

import com.RotaDurak.RotaDurak.model.*;
import com.RotaDurak.RotaDurak.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Transactional
public class GtfsImportService {
    @Autowired private CityRepository cityRepository;
    @Autowired private StationRepository stationRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private RouteStationRepository routeStationRepository;
    @Autowired private MovementTimeRepository movementTimeRepository;
    @Autowired private ShapePointRepository shapePointRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    public void importGtfs(MultipartFile file, String cityName) throws IOException {
        // başlangıç log
        System.out.println(">>> importGtfs çağrıldı, cityName = " + cityName + ", file = " + file.getOriginalFilename());

        // şehir kontrolü
        City city = cityRepository.findByName(cityName);
        if (city == null) throw new IllegalArgumentException("City '" + cityName + "' bulunamadı.");

        // sequence reset
        resetSequence("route", "routeid");
        resetSequence("station", "stationid");
        resetSequence("movementtime", "movementid");
        resetSequence("shapepoint", "shapepointid");

        // geçici ZIP dosyası
        File tmp = File.createTempFile("gtfs-", ".zip");
        file.transferTo(tmp);

        // yardımcı haritalar
        Map<String, Route> gtfsRouteMap = new HashMap<>();
        Map<String, Station> gtfsStationMap = new HashMap<>();
        Map<String, Pair<Route, Direction>> tripInfoMap = new HashMap<>();
        Map<String, Pair<Integer, LocalTime>> earliestDeparture = new HashMap<>();
        Map<String, Route> shapeRouteMap = new HashMap<>();        // shapeId -> Route map
        Set<String> seenRouteStationKeys = new HashSet<>();

        ZipFile zipFile = new ZipFile(tmp);
        try {
            // 1) routes.txt
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().toLowerCase().endsWith("routes.txt")) {
                    try (InputStream is = zipFile.getInputStream(entry);
                         CSVParser parser = CSVFormat.DEFAULT
                                 .withFirstRecordAsHeader()
                                 .parse(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        for (CSVRecord rec : parser) {
                            String rtId = rec.get("route_id").trim();
                            String shortName = rec.get("route_short_name").trim();
                            String longName = rec.get("route_long_name").trim();
                            Route route = routeRepository.findByLineAndName(shortName, longName)
                                    .orElseGet(() -> routeRepository.save(
                                            new Route(null, shortName, longName, city, null, new HashSet<>())
                                    ));
                            gtfsRouteMap.put(rtId, route);
                        }
                    }
                }
            }
            // 2) stops.txt
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().toLowerCase().endsWith("stops.txt")) {
                    try (InputStream is = zipFile.getInputStream(entry);
                         CSVParser parser = CSVFormat.DEFAULT
                                 .withFirstRecordAsHeader()
                                 .parse(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        for (CSVRecord rec : parser) {
                            String stopId = rec.get("stop_id").trim();
                            String stopName = rec.get("stop_name").trim();
                            String loc = rec.get("stop_lat").trim() + "," + rec.get("stop_lon").trim();
                            Station station = stationRepository.findByNameAndLocation(stopName, loc)
                                    .orElseGet(() -> stationRepository.save(
                                            new Station(null, stopName, loc, city, new HashSet<>())
                                    ));
                            gtfsStationMap.put(stopId, station);
                        }
                    }
                }
            }
            // 3) trips.txt
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().toLowerCase().endsWith("trips.txt")) {
                    try (InputStream is = zipFile.getInputStream(entry);
                         CSVParser parser = CSVFormat.DEFAULT
                                 .withFirstRecordAsHeader()
                                 .parse(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        for (CSVRecord rec : parser) {
                            String tripId = rec.get("trip_id").trim();
                            String rtId = rec.get("route_id").trim();
                            String dir = rec.get("direction_id").trim();
                            String shapeId = rec.get("shape_id").trim();                 // shape_id from GTFS

                            Route route = gtfsRouteMap.get(rtId);
                            if (route != null) {
                                // map shape to route
                                shapeRouteMap.put(shapeId, route);
                            }

                            Direction d = "1".equals(dir) ? Direction.endToStart : Direction.startToEnd;
                            tripInfoMap.put(tripId, Pair.of(route, d));
                        }
                    }
                }
            }
            // 4) stop_times.txt
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().toLowerCase().endsWith("stop_times.txt")) {
                    try (InputStream is = zipFile.getInputStream(entry);
                         CSVParser parser = CSVFormat.DEFAULT
                                 .withFirstRecordAsHeader()
                                 .parse(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        for (CSVRecord rec : parser) {
                            String tripId = rec.get("trip_id").trim();
                            int seq = Integer.parseInt(rec.get("stop_sequence").trim());
                            String[] tm = rec.get("departure_time").trim().split(":");
                            int hr = Integer.parseInt(tm[0]); if (hr >= 24) hr -= 24;
                            LocalTime dep = LocalTime.of(hr,
                                    Integer.parseInt(tm[1]), Integer.parseInt(tm[2]));

                            Pair<Integer, LocalTime> cur = earliestDeparture.get(tripId);
                            if (cur == null || seq < cur.getFirst()) {
                                earliestDeparture.put(tripId, Pair.of(seq, dep));
                            }

                            // route-station relationship
                            Pair<Route, Direction> info = tripInfoMap.get(tripId);
                            if (info != null) {
                                Route route = info.getFirst();
                                Station station = gtfsStationMap.get(rec.get("stop_id").trim());
                                String key = route.getId() + "_" + station.getId();
                                if (seenRouteStationKeys.add(key)) {
                                    routeStationRepository.save(
                                            new RouteStation(null, route, station)
                                    );
                                }
                            }
                        }
                    }
                }
            }
            // 5) shapes.txt
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().toLowerCase().endsWith("shapes.txt")) {
                    try (InputStream is = zipFile.getInputStream(entry);
                         CSVParser parser = CSVFormat.DEFAULT
                                 .withFirstRecordAsHeader()
                                 .parse(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        for (CSVRecord rec : parser) {
                            String shapeId = rec.get("shape_id").trim();
                            int seq = Integer.parseInt(rec.get("shape_pt_sequence").trim());
                            double lat = Double.parseDouble(rec.get("shape_pt_lat").trim());
                            double lon = Double.parseDouble(rec.get("shape_pt_lon").trim());
                            Double dist = rec.isMapped("shape_dist_traveled") && !rec.get("shape_dist_traveled").isEmpty()
                                    ? Double.parseDouble(rec.get("shape_dist_traveled").trim()) : null;

                            // assign shape to route if not set
                            Route route = shapeRouteMap.get(shapeId);
                            if (route != null && route.getShape() == null) {
                                route.setShape(shapeId);
                                routeRepository.save(route);
                            }

                            // save shape point
                            shapePointRepository.save(
                                    new ShapePoint(null, shapeId, seq, lat, lon, dist)
                            );
                        }
                    }
                }
            }
        } finally {
            zipFile.close();
            tmp.delete();
        }
        // 6) MovementTime kayıtları
        for (Map.Entry<String, Pair<Integer, LocalTime>> e : earliestDeparture.entrySet()) {
            Pair<Route, Direction> info = tripInfoMap.get(e.getKey());
            if (info != null) {
                MovementTime mt = new MovementTime();
                mt.setRoute(info.getFirst());
                mt.setDirection(info.getSecond());
                mt.setTime(e.getValue().getSecond());
                movementTimeRepository.save(mt);
            }
        }
    }

    private void resetSequence(String table, String column) {
        String seq = jdbcTemplate.queryForObject(
                String.format("SELECT pg_get_serial_sequence('%s','%s')", table, column),
                String.class
        );
        jdbcTemplate.execute(
                String.format(
                        "SELECT setval('%s',(SELECT COALESCE(MAX(%s),1) FROM %s))",
                        seq, column, table
                )
        );
    }
}
