package com.RotaDurak.RotaDurak.controller;
import com.RotaDurak.RotaDurak.model.PopularPlace;
import com.RotaDurak.RotaDurak.service.PopularPlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/popularplaces")
public class PopularPlaceController {
    @Autowired
    private PopularPlaceService popularPlaceService;

    @GetMapping
    public List<PopularPlace> getAllPopularPlaces() {
        return popularPlaceService.getAllPopularPlaces();
    }

    @GetMapping("/{id}")
    public Optional<PopularPlace> getPopularPlaceById(@PathVariable Long id) {
        return popularPlaceService.getPopularPlaceById(id);
    }

    @PostMapping
    public PopularPlace createPopularPlace(@RequestBody PopularPlace place) {
        return popularPlaceService.createPopularPlace(place);
    }
}
