package com.RotaDurak.RotaDurak.controller;

import com.RotaDurak.RotaDurak.model.MovementTime;
import com.RotaDurak.RotaDurak.service.MovementTimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movement")
public class MovementTimeController {
    private final MovementTimeService movementTimeService;

    public MovementTimeController(MovementTimeService movementTimeService) {
        this.movementTimeService = movementTimeService;
    }

    @GetMapping
    public List<MovementTime> getAllMovementTimes() {
        return movementTimeService.getAllMovementTimes();
    }
}
