package com.RotaDurak.RotaDurak.service;

import org.springframework.stereotype.Service;
import com.RotaDurak.RotaDurak.model.MovementTime;
import com.RotaDurak.RotaDurak.repository.MovementTimeRepository;
import java.util.List;

@Service
public class MovementTimeService {
    private final MovementTimeRepository movementTimeRepository;

    public MovementTimeService(MovementTimeRepository movementTimeRepository) {
        this.movementTimeRepository = movementTimeRepository;
    }

    public List<MovementTime> getAllMovementTimes() {
        return movementTimeRepository.findAll();
    }

}
