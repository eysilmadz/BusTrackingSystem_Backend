package com.RotaDurak.RotaDurak.gps.parser;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Gt06Parser {
    public Map<String, String> parse(byte[] data) {
        Map<String, String> result = new HashMap<>();

        if (data == null || data.length < 2) return result;

        // Login paketi: 0x78 0x78 ile başlar, protocol 0x01
        if (data[0] == 0x78 && data[1] == 0x78 && data.length > 4 && data[3] == 0x01) {
            // IMEI: 8 byte, her byte'ın iki hanesi bir rakam
            if (data.length >= 13) {
                StringBuilder imei = new StringBuilder();
                for (int i = 4; i < 12; i++) {
                    int b = data[i] & 0xFF;
                    imei.append((b >> 4) & 0x0F);
                    imei.append(b & 0x0F);
                }
                // Son byte'ın ilk hanesi
                imei.append((data[12] >> 4) & 0x0F);
                result.put("type", "login");
                result.put("imei", imei.toString());
            }
            return result;
        }

        // GPS paketi: 0x78 0x78 ile başlar, protocol 0x12
        if (data[0] == 0x78 && data[1] == 0x78 && data.length > 4 && data[3] == 0x12) {
            try {
                // Lat: 4 byte, index 4
                long latRaw = ((data[4] & 0xFFL) << 24) |
                        ((data[5] & 0xFFL) << 16) |
                        ((data[6] & 0xFFL) << 8)  |
                        (data[7] & 0xFFL);
                double lat = latRaw / 1800000.0;

                // Lon: 4 byte, index 8
                long lonRaw = ((data[8]  & 0xFFL) << 24) |
                        ((data[9]  & 0xFFL) << 16) |
                        ((data[10] & 0xFFL) << 8)  |
                        (data[11] & 0xFFL);
                double lon = lonRaw / 1800000.0;

                // Speed: 1 byte, index 12
                int speed = data[12] & 0xFF;

                // flags byte: index 13
                int flags = data[13] & 0xFF;
                boolean latSouth = (flags & 0x04) == 0;
                boolean lonWest  = (flags & 0x08) == 0;

                if (latSouth) lat = -lat;
                if (lonWest)  lon = -lon;

                result.put("type", "gps");
                result.put("lat", String.valueOf(lat));
                result.put("lon", String.valueOf(lon));
                result.put("speed", String.valueOf(speed));
            } catch (Exception e) {
                System.out.println("❌ GPS parse error: " + e.getMessage());
            }
            return result;
        }

        return result;
    }
}


