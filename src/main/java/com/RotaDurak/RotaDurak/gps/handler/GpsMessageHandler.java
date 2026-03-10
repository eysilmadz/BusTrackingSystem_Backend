package com.RotaDurak.RotaDurak.gps.handler;


import com.RotaDurak.RotaDurak.dto.PositionMessage;
import com.RotaDurak.RotaDurak.gps.parser.Gt06Parser;
import com.RotaDurak.RotaDurak.repository.GpsDeviceRepository;
import com.RotaDurak.RotaDurak.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GpsMessageHandler {
    private final GpsDeviceRepository gpsDeviceRepository;
    private final Gt06Parser parser;
    private final KafkaProducerService kafkaProducerService;
    private final SimpMessagingTemplate messagingTemplate;

    public void handle(byte[] data, Socket socket) {
        Map<String, String> parsed = parser.parse(data);

        if (parsed.isEmpty()) {
            System.out.println("⚠️ Unknown packet");
            return;
        }

        String type = parsed.get("type");

        // Login paketi — cihaz bağlandığında IMEI gönderir, ACK döneriz
        if ("login".equals(type)) {
            String imei = parsed.get("imei");
            System.out.println("🔑 LOGIN | IMEI=" + imei);
            sendLoginAck(socket);
            return;
        }

        // GPS paketi
        if ("gps".equals(type)) {
            // Bu pakette IMEI yok, önceki login'den biliyoruz
            // Şimdilik sabit IMEI ile test edelim
            String imei = "867144060044995";

            double lat   = Double.parseDouble(parsed.get("lat"));
            double lon   = Double.parseDouble(parsed.get("lon"));
            int speed    = Integer.parseInt(parsed.getOrDefault("speed", "0"));

            System.out.println("📍 GPS | LAT=" + lat + " | LON=" + lon + " | SPEED=" + speed);

            gpsDeviceRepository.findByImei(imei).ifPresentOrElse(device -> {

                device.setLastSeenAt(LocalDateTime.now());
                gpsDeviceRepository.save(device);

                PositionMessage message = new PositionMessage(
                        device.getRoute().getId(),
                        lat, lon,
                        Instant.now(),
                        speed,
                        imei,
                        null
                );
                kafkaProducerService.send(message);

                System.out.println("✅ SENT TO KAFKA | IMEI=" + imei +
                        " | LAT=" + lat + " | LON=" + lon);

            }, () -> System.out.println("❌ Unknown IMEI: " + imei));
        }
    }

    // GT06 login ACK: 78 78 05 01 [serial] [checksum] 0D 0A
    private void sendLoginAck(Socket socket) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(new byte[]{(byte)0x78, (byte)0x78, (byte)0x05, (byte)0x01, (byte)0x00, (byte)0x01, (byte)0xD9, (byte)0xDC, (byte)0x0D, (byte)0x0A});
            out.flush();
            System.out.println("✅ Login ACK sent");
        } catch (Exception e) {
            System.out.println("❌ ACK error: " + e.getMessage());
        }
    }

}






