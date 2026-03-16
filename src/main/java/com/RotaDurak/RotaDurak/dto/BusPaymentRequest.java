package com.RotaDurak.RotaDurak.dto;

import lombok.Data;

@Data
public class BusPaymentRequest {
    private Long userId;
    private Double amount;
    private String busLine;      // "34 Kadıköy"
    private String paymentMethod; // "NFC" | "QR" | "VIRTUAL_CARD"
    private String token;        // NFC token veya QR code değeri
    private String qrData;        // QR ile ödemede → JSON string
}
