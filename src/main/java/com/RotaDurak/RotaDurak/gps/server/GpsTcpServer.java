package com.RotaDurak.RotaDurak.gps.server;
import com.RotaDurak.RotaDurak.gps.handler.GpsMessageHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GpsTcpServer {
    private static final int PORT = 5000; // GPS cihazlarının bağlanacağı port

    private final GpsMessageHandler handler;

    @PostConstruct
    public void startServer() {
        // Spring ayağa kalkınca TCP server'ı ayrı bir thread'de başlatıyoruz
        new Thread(this::runServer, "gps-tcp-server").start();
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 GPS TCP Server listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("📡 GPS device connected: " + socket.getRemoteSocketAddress());

                // Her GPS cihazı için ayrı thread
                new Thread(() -> handleClient(socket), "gps-client-handler").start();
            }

        } catch (Exception e) {
            System.err.println("❌ GPS TCP Server error");
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (InputStream inputStream = socket.getInputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {

                String rawMessage = new String(
                        buffer,
                        0,
                        bytesRead,
                        StandardCharsets.UTF_8
                ).trim();

                if (rawMessage.isEmpty()) {
                    continue;
                }

                System.out.println("📥 RAW GPS DATA: " + rawMessage);

                // 🔴 ASIL ÖNEMLİ NOKTA
                // TCP server iş yapmaz, sadece handler'a paslar
                handler.handle(rawMessage);
            }

        } catch (Exception e) {
            System.out.println("❌ GPS client disconnected: " + socket.getRemoteSocketAddress());
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }
}