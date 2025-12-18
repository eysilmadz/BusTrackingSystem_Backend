package com.RotaDurak.RotaDurak.dto;

import com.RotaDurak.RotaDurak.model.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceStatusMessage {
    private String imei;
    private Long routeId;
    private DeviceStatus status;
}
