package com.RotaDurak.RotaDurak.service;
import com.RotaDurak.RotaDurak.model.PopularPlace;
import com.RotaDurak.RotaDurak.repository.PopularPlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PopularPlaceService {
    @Autowired
    private PopularPlaceRepository popularPlaceRepository;

    public List<PopularPlace> getAllPopularPlaces() {
        return popularPlaceRepository.findAll();
    }

    public Optional<PopularPlace> getPopularPlaceById(Long id) {
        return popularPlaceRepository.findById(id);
    }

    public PopularPlace createPopularPlace(PopularPlace place) {
        return popularPlaceRepository.save(place);
    }

    public List<PopularPlace> getPopularPlacesByCityId(Long cityId) {
        return popularPlaceRepository.findByCity_Id(cityId);
    }
}
