package com.RotaDurak.RotaDurak.controller;
import com.RotaDurak.RotaDurak.model.City;
import com.RotaDurak.RotaDurak.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.RotaDurak.RotaDurak.dto.CityDto;
import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {
    @Autowired
    private CityService cityService;

    @GetMapping("/names") // Tüm şehirleri getirir (id + name)
    public List<CityDto> getAllCities() {
        return cityService.getAllCities();
    }


}
