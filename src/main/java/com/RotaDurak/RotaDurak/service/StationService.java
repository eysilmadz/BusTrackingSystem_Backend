package com.RotaDurak.RotaDurak.service;
import com.RotaDurak.RotaDurak.model.Station;
import com.RotaDurak.RotaDurak.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StationService {
    @Autowired
    private StationRepository stationRepository;

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getStationById(Long id) {
        return stationRepository.findById(id);
    }

    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    public List<Station> getStationsByCityId(Long cityId) {
        return stationRepository.findAllByCity_Id(cityId);
    }
}
