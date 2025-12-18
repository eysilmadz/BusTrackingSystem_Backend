package com.RotaDurak.RotaDurak.gps.handler;

import com.RotaDurak.RotaDurak.dto.PositionMessage;
import com.RotaDurak.RotaDurak.gps.parser.MockGpsParser;
import com.RotaDurak.RotaDurak.model.DeviceStatus;
import com.RotaDurak.RotaDurak.repository.GpsDeviceRepository;
import com.RotaDurak.RotaDurak.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GpsMessageHandler {
    private final GpsDeviceRepository gpsDeviceRepository;
    private final MockGpsParser parser;
    private final KafkaProducerService kafkaProducerService;

    public void handle(String rawMessage) {

        Map<String, String> data = parser.parse(rawMessage);

        String imei = data.get("imei");
        if (imei == null) {
            System.out.println("❌ IMEI not found");
            return;
        }

        gpsDeviceRepository.findByImei(imei).ifPresentOrElse(device -> {

            if (device.getStatus() != DeviceStatus.ACTIVE) {
                System.out.println("🚫 BLOCKED DEVICE: " + imei);
                return;
            }

            // heartbeat
            device.setLastSeenAt(LocalDateTime.now());
            gpsDeviceRepository.save(device);

            // güvenli parse
            double latitude  = Double.parseDouble(data.get("lat"));
            double longitude = Double.parseDouble(data.get("lon"));
            Integer speed    = Integer.parseInt(data.getOrDefault("speed", "0"));

            PositionMessage message = new PositionMessage(
                    device.getRoute().getId(), // routeId
                    latitude,                  // latitude
                    longitude,                 // longitude
                    Instant.now(),             // timestamp
                    speed,                     // speed
                    imei,                      // imei
                    null                       // direction (şimdilik yok)
            );

            kafkaProducerService.send(message);

            System.out.println(
                    "✅ SENT TO KAFKA | IMEI=" + imei +
                            " | ROUTE=" + device.getRoute().getId() +
                            " | LAT=" + latitude +
                            " | LON=" + longitude
            );

        }, () -> {
            System.out.println("❌ Unknown IMEI: " + imei);
        });
    }

}






