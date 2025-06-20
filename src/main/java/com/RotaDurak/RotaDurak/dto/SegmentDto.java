package com.RotaDurak.RotaDurak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SegmentDto {
    private long fromStationId;
    private long toStationId;
    private String mode;
    private String routeLine;
    private double durationMin;
    private double distanceKm;
}
