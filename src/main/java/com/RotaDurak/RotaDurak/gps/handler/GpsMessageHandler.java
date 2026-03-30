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
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GpsMessageHandler {
    private final GpsDeviceRepository gpsDeviceRepository;
    private final Gt06Parser parser;
    private final KafkaProducerService kafkaProducerService;

    // Socket başına IMEI sakla
    private final Map<Socket, String> socketImeiMap = new ConcurrentHashMap<>();

    public void handle(byte[] data, Socket socket) {
        Map<String, String> parsed = parser.parse(data);

        if (parsed.isEmpty()) {
            System.out.println("⚠️ Unknown packet");
            return;
        }

        String type = parsed.get("type");

        if ("login".equals(type)) {
            String imei = parsed.get("imei");
            socketImeiMap.put(socket, imei);
            System.out.println("🔑 LOGIN | IMEI=" + imei);
            sendLoginAck(socket, parsed.getOrDefault("serial", "1"));
            return;
        }

        if ("heartbeat".equals(type)) {
            System.out.println("💓 Heartbeat | protocol=" + parsed.get("protocol"));
            return;
        }

        if ("gps".equals(type)) {
            String imei = socketImeiMap.get(socket);
            if (imei == null) {
                System.out.println("⚠️ GPS packet before login, ignoring");
                return;
            }

            boolean fixed = Boolean.parseBoolean(parsed.getOrDefault("fixed", "false"));
            if (!fixed) {
                System.out.println("📡 GPS UNFIXED | IMEI=" + imei + " (uydu sinyali yok)");
                return;
            }

            double lat   = Double.parseDouble(parsed.get("lat"));
            double lon   = Double.parseDouble(parsed.get("lon"));
            int speed    = Integer.parseInt(parsed.getOrDefault("speed", "0"));
            String dt    = parsed.getOrDefault("datetime", "");

            System.out.println("📍 GPS | IMEI=" + imei + " | LAT=" + lat +
                    " | LON=" + lon + " | SPD=" + speed + " | " + dt);

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
                System.out.println("✅ SENT TO KAFKA | IMEI=" + imei);

            }, () -> System.out.println("❌ Unknown IMEI: " + imei));
        }
    }

    public void onDisconnect(Socket socket) {
        String imei = socketImeiMap.remove(socket);
        System.out.println("🔌 Disconnected | IMEI=" + (imei != null ? imei : "unknown"));
    }

    private void sendLoginAck(Socket socket, String serialStr) {
        try {
            int serial = Integer.parseInt(serialStr);
            byte s1 = (byte)((serial >> 8) & 0xFF);
            byte s2 = (byte)(serial & 0xFF);

            // Checksum: XOR of bytes between start and checksum
            byte[] packet = {0x05, 0x01, s1, s2};
            int crc = 0;
            for (byte b : packet) crc ^= (b & 0xFF);

            OutputStream out = socket.getOutputStream();
            out.write(new byte[]{
                    (byte)0x78, (byte)0x78,
                    0x05, 0x01,
                    s1, s2,
                    (byte)((crc >> 8) & 0xFF), (byte)(crc & 0xFF),
                    0x0D, 0x0A
            });
            out.flush();
            System.out.println("✅ Login ACK sent | serial=" + serial);
        } catch (Exception e) {
            System.out.println("❌ ACK error: " + e.getMessage());
        }
    }
}





