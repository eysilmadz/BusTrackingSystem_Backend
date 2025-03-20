package com.RotaDurak.RotaDurak.service;
import com.RotaDurak.RotaDurak.model.City;
import com.RotaDurak.RotaDurak.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CityService {
    @Autowired
    private CityRepository cityRepository;

    public List<String> getAllCityNames() {
        return cityRepository.findAllCityNames();
    }
}
