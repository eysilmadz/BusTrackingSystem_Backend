package com.RotaDurak.RotaDurak.service;
import com.RotaDurak.RotaDurak.dto.CityDto;
import com.RotaDurak.RotaDurak.model.City;
import com.RotaDurak.RotaDurak.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CityService {
    @Autowired
    private CityRepository cityRepository;

    public List<CityDto> getAllCities() {
        List<City> cities = cityRepository.findAll();
        return cities.stream()
                .map(city -> new CityDto(city.getId(), city.getName()))
                .collect(Collectors.toList());
    }
}
