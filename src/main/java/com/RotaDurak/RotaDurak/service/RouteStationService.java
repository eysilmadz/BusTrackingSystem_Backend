package com.RotaDurak.RotaDurak.service;

import com.RotaDurak.RotaDurak.model.Route;
import com.RotaDurak.RotaDurak.model.RouteStation;
import com.RotaDurak.RotaDurak.model.Station;
import com.RotaDurak.RotaDurak.repository.RouteStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteStationService {
    @Autowired
    private RouteStationRepository routeStationRepository;

    public List<Station> getRouteStationsByRoute(Long routeId) {
        List<RouteStation> routeStations = routeStationRepository.findByRouteId(routeId);
        List<Station> stations = new ArrayList<Station>();
        for (RouteStation routeStation : routeStations) {
            stations.add(routeStation.getStation());
        }
    return stations;
    }

    public List<Route> getRoutesByStation(Long stationId) {
        List<RouteStation> routeStations = routeStationRepository.findByStationId(stationId);
        List<Route> routes = new ArrayList<>();
        for(RouteStation routeStation : routeStations){
            routes.add(routeStation.getRoute());
        }
        return routes;
    }

}
