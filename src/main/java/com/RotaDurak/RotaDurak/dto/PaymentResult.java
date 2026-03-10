package com.RotaDurak.RotaDurak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResult {
    private Boolean success;
    private String message;
    private String referenceCode;
}
