package com.RotaDurak.RotaDurak.dto;

import com.RotaDurak.RotaDurak.model.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementTimeDto {
    private Direction direction;
    private String time;
}
