package com.RotaDurak.RotaDurak.controller;
import com.RotaDurak.RotaDurak.model.City;
import com.RotaDurak.RotaDurak.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cities")
public class CityController {
    @Autowired
    private CityService cityService;

    @GetMapping("/names") //Tüm şehirleri getirir
    public List<String> getAllCityNames() {
        return cityService.getAllCityNames();
    }


}
