package com.RotaDurak.RotaDurak.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.RotaDurak.RotaDurak.model.MovementTime;
import com.RotaDurak.RotaDurak.repository.MovementTimeRepository;

import java.util.List;

@Service
public class MovementTimeService {
    @Autowired
    private final MovementTimeRepository movementTimeRepository;
    @Autowired
    private ApplicationEventPublisher publisher;

    public MovementTimeService(MovementTimeRepository movementTimeRepository) {
        this.movementTimeRepository = movementTimeRepository;
    }

    public List<MovementTime> getMovementTimesByRoute(Long routeId) {

        return movementTimeRepository.findByRouteId(routeId);
    }

}

