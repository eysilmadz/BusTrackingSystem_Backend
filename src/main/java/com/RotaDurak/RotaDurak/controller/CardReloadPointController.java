package com.RotaDurak.RotaDurak.controller;

import com.RotaDurak.RotaDurak.model.CardReloadPoint;
import com.RotaDurak.RotaDurak.service.CardReloadPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reloadpoints")
public class CardReloadPointController {
    @Autowired
    private CardReloadPointService cardReloadPointService;

    @GetMapping
    public List<CardReloadPoint> getAllReloadPoints() {
        return cardReloadPointService.getAllReloadPoints();
    }

    @GetMapping("/{id}")
    public Optional<CardReloadPoint> getReloadPointById(@PathVariable Long id) {
        return cardReloadPointService.getReloadPointById(id);
    }

    @PostMapping
    public CardReloadPoint createReloadPoint(@RequestBody CardReloadPoint reloadPoint) {
        return cardReloadPointService.createReloadPoint(reloadPoint);
    }
}
