package com.RotaDurak.RotaDurak.controller;

import com.RotaDurak.RotaDurak.dto.MovementTimeDTO;
import com.RotaDurak.RotaDurak.model.MovementTime;
import com.RotaDurak.RotaDurak.service.MovementTimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movement")
public class MovementTimeController {
    private final MovementTimeService movementTimeService;

    public MovementTimeController(MovementTimeService movementTimeService) {
        this.movementTimeService = movementTimeService;
    }

    @GetMapping("/byRoute/{routeId}")
    public List<MovementTimeDTO> getMovementTimesByRoute(@PathVariable Long routeId) {
        return movementTimeService.getMovementTimesByRoute(routeId)
                .stream()
                .map(mt -> new MovementTimeDTO(
                        mt.getDirection(),
                        mt.getTime().toString()))
                .collect(Collectors.toList());
    }
}
