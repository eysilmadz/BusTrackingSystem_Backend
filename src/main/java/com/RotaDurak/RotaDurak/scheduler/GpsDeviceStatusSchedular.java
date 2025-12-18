package com.RotaDurak.RotaDurak.scheduler;

import com.RotaDurak.RotaDurak.dto.DeviceStatusMessage;
import com.RotaDurak.RotaDurak.model.DeviceStatus;
import com.RotaDurak.RotaDurak.model.GPSDevice;
import com.RotaDurak.RotaDurak.repository.GpsDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GpsDeviceStatusSchedular {
    private static final int OFFLINE_THRESHOLD_SECONDS = 60;

    private final GpsDeviceRepository gpsDeviceRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelay = 30_000) //30 saniyede bir
    public void checkOfflineDevices() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(OFFLINE_THRESHOLD_SECONDS);

        List<GPSDevice> onlineDevices = gpsDeviceRepository.findByStatus(DeviceStatus.ONLINE);

        for(GPSDevice device : onlineDevices) {

            if (device.getLastSeenAt() == null ||
                    device.getLastSeenAt().isBefore(threshold)) {

                device.setStatus(DeviceStatus.OFFLINE);
                gpsDeviceRepository.save(device);

                // 🔔 WebSocket bildirimi
                DeviceStatusMessage statusMessage =
                        new DeviceStatusMessage(
                                device.getImei(),
                                device.getRoute().getId(),
                                DeviceStatus.OFFLINE
                        );

                messagingTemplate.convertAndSend(
                        "/topic/device-status/" + device.getRoute().getId(),
                        statusMessage
                );

                System.out.println(
                        "🔴 GPS OFFLINE | IMEI=" + device.getImei()
                );
            }
        }
    }


}
