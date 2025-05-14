package com.RotaDurak.RotaDurak.dto;

import com.RotaDurak.RotaDurak.model.Direction;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionMessage {
    private Long routeId;
    private double latitude;
    private double longitude;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
    Direction direction;
}
