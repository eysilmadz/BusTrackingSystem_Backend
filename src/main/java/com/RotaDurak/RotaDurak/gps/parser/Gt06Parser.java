package com.RotaDurak.RotaDurak.gps.parser;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Gt06Parser {

    public Map<String, String> parse(byte[] data) {
        Map<String, String> result = new HashMap<>();
        if (data == null || data.length < 4) return result;

        // === KISA PAKET: 0x78 0x78 ===
        if (data[0] == 0x78 && data[1] == 0x78) {
            int protocol = data[3] & 0xFF;

            if (protocol == 0x01 && data.length >= 12) {
                StringBuilder imei = new StringBuilder();
                for (int i = 4; i < 12; i++) {
                    int b = data[i] & 0xFF;
                    imei.append((b >> 4) & 0x0F);
                    imei.append(b & 0x0F);
                }
                String imeiStr = imei.toString(); // 16 hane
                if (imeiStr.length() >= 16) {
                    imeiStr = imeiStr.substring(1, 16); // başındaki 0'ı at → 15 hane
                }
                // serial number
                int serial = ((data[data.length - 4] & 0xFF) << 8) | (data[data.length - 3] & 0xFF);
                result.put("type", "login");
                result.put("imei", imeiStr);
                result.put("serial", String.valueOf(serial));
                return result;
            }

            // GPS paketi 0x12
            if (protocol == 0x12) {
                return parseGpsData(data, 4, result);
            }

            // GPS paketi 0x13 (bazı GT06 versiyonları)
            if (protocol == 0x13) {
                return parseGpsData(data, 4, result);
            }

            // Protocol 0x22 — GPS+LBS kombine paket
            if (data[0] == 0x78 && data[1] == 0x78 && data[3] == 0x22) {
                return parseGpsData(data, 4, result);
            }

// Protocol 0x34 — alternatif GPS+LBS
            if (data[0] == 0x78 && data[1] == 0x78 && data[3] == 0x34) {
                return parseGpsData(data, 4, result);
            }

            // Heartbeat / diğer paketleri sessizce geç
            result.put("type", "heartbeat");
            result.put("protocol", String.valueOf(protocol));
            return result;
        }

        // === UZUN PAKET: 0x79 0x79 ===
        if (data[0] == 0x79 && data[1] == 0x79) {
            // length 2 byte, protocol index 4
            if (data.length < 5) return result;
            int protocol = data[4] & 0xFF;
            if (protocol == 0x12 || protocol == 0x13) {
                return parseGpsData(data, 5, result);
            }
            result.put("type", "heartbeat");
            return result;
        }

        return result;
    }

    private Map<String, String> parseGpsData(byte[] data, int offset, Map<String, String> result) {
        try {
            // GT06 GPS data layout (offset'ten itibaren):
            // [0..5] = tarih/saat (yy mm dd hh mi ss)
            // [6]    = GPS info byte (uydu sayısı vs.)
            // [7..10] = latitude (4 byte)
            // [11..14] = longitude (4 byte)
            // [15]   = speed
            // [16..17] = flags + course

            if (data.length < offset + 16) {
                System.out.println("⚠️ GPS packet too short: " + data.length);
                return result;
            }

            // Tarih/saat
            int yy = data[offset]     & 0xFF;
            int mo = data[offset + 1] & 0xFF;
            int dd = data[offset + 2] & 0xFF;
            int hh = data[offset + 3] & 0xFF;
            int mi = data[offset + 4] & 0xFF;
            int ss = data[offset + 5] & 0xFF;

            // Latitude
            long latRaw = ((data[offset + 7]  & 0xFFL) << 24) |
                    ((data[offset + 8]  & 0xFFL) << 16) |
                    ((data[offset + 9]  & 0xFFL) << 8)  |
                    (data[offset + 10] & 0xFFL);
            double lat = latRaw / 1800000.0;

            // Longitude
            long lonRaw = ((data[offset + 11] & 0xFFL) << 24) |
                    ((data[offset + 12] & 0xFFL) << 16) |
                    ((data[offset + 13] & 0xFFL) << 8)  |
                    (data[offset + 14] & 0xFFL);
            double lon = lonRaw / 1800000.0;

            // Speed
            int speed = data[offset + 15] & 0xFF;

            // Flags (kuzey/güney, doğu/batı)
            int flags = 0;
            if (data.length > offset + 16) {
                flags = ((data[offset + 16] & 0xFF) << 8) | (data[offset + 17] & 0xFF);
            }
            boolean latNorth = (flags & 0x0400) != 0;
            boolean lonEast  = (flags & 0x0800) != 0;

            if (!latNorth) lat = -lat;
            if (!lonEast)  lon = -lon;

            // GPS fixed mi?
            int gpsInfo = data[offset + 6] & 0xFF;
            int satellites = gpsInfo & 0x0F;
            boolean fixed = satellites > 0;

            // Fix kontrolü — flags'in 12. biti
            boolean gpsFixed = (flags & 0x1000) != 0;
// Alternatif: speed mantıksızsa da geçersiz say
            if (!gpsFixed || speed > 200) {
                result.put("type", "gps_unfixed");
                System.out.println("📡 GPS UNFIXED | satellites=" + satellites);
                return result;
            }

            result.put("type", "gps");
            result.put("lat", String.valueOf(lat));
            result.put("lon", String.valueOf(lon));
            result.put("speed", String.valueOf(speed));
            result.put("satellites", String.valueOf(satellites));
            result.put("fixed", String.valueOf(fixed));
            result.put("datetime", String.format("20%02d-%02d-%02d %02d:%02d:%02d", yy, mo, dd, hh, mi, ss));

        } catch (Exception e) {
            System.out.println("❌ GPS parse error: " + e.getMessage());
        }
        return result;
    }
}

