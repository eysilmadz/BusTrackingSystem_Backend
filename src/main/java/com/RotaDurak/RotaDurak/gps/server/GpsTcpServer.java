package com.RotaDurak.RotaDurak.gps.server;

import com.RotaDurak.RotaDurak.gps.handler.GpsMessageHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Component
@RequiredArgsConstructor
public class GpsTcpServer {
    private static final int PORT = 5000;
    private final GpsMessageHandler handler;

    @PostConstruct
    public void startServer() {
        new Thread(this::runServer, "gps-tcp-server").start();
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 GPS TCP Server listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("📡 GPS device connected: " + socket.getRemoteSocketAddress());
                new Thread(() -> handleClient(socket), "gps-client-handler").start();
            }
        } catch (Exception e) {
            System.err.println("❌ GPS TCP Server error");
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (InputStream in = socket.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                byte[] data = new byte[bytesRead];
                System.arraycopy(buffer, 0, data, 0, bytesRead);

                // HEX log
                StringBuilder hex = new StringBuilder();
                for (byte b : data) hex.append(String.format("%02X ", b));
                System.out.println("📥 RAW HEX: " + hex);

                handler.handle(data, socket);
            }
        } catch (Exception e) {
            System.out.println("❌ GPS client disconnected: " + socket.getRemoteSocketAddress());
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}