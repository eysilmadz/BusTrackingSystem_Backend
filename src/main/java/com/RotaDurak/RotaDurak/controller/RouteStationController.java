package com.RotaDurak.RotaDurak.controller;

import com.RotaDurak.RotaDurak.model.Route;
import com.RotaDurak.RotaDurak.model.Station;
import com.RotaDurak.RotaDurak.service.RouteStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routeStations")
public class RouteStationController {

    @Autowired
    private RouteStationService routeStationService;

    @GetMapping("/byRoute/{routeId}")
    public List<Station> getRouteStationsByRoute(@PathVariable Long routeId) {
        return routeStationService.getRouteStationsByRoute(routeId);
    }

    @GetMapping("/byStation/{stationId}")
    public List<Route> getRoutesByStation(@PathVariable Long stationId) {
        return routeStationService.getRoutesByStation(stationId);
    }

}