package com.RotaDurak.RotaDurak.controller;
import com.RotaDurak.RotaDurak.model.Station;
import com.RotaDurak.RotaDurak.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stations")
public class StationController {
    @Autowired
    private StationService stationService;

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{id}")
    public Optional<Station> getStationById(@PathVariable Long id) {
        return stationService.getStationById(id);
    }

    @PostMapping
    public Station createStation(@RequestBody Station station) {
        return stationService.createStation(station);
    }
}
