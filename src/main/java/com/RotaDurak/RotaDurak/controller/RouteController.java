package com.RotaDurak.RotaDurak.controller;
import com.RotaDurak.RotaDurak.model.Route;
import com.RotaDurak.RotaDurak.model.RouteStation;
import com.RotaDurak.RotaDurak.model.Station;
import com.RotaDurak.RotaDurak.service.RouteService;
import com.RotaDurak.RotaDurak.service.RouteStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    @Autowired
    private RouteService routeService;

    //Belirli bir şehre ait verileri getirir
    @GetMapping("/byCityId")
    public List<Route> getRoutesByCityId(@RequestParam Long cityId) {
        return routeService.getRoutesByCityId(cityId);
    }

    //Belirli bir hat'a ait durakları getirir
    @GetMapping("/{routeId}/stations")
    public List<Station> getRouteStations(@PathVariable Long routeId) {
        return routeService.getRouteStations(routeId);
    }
}
