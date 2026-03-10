package com.RotaDurak.RotaDurak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CheckoutFormRequest {
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("amount")
    private Double amount;
}
