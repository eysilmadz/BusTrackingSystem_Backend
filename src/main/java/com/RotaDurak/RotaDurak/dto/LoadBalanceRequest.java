package com.RotaDurak.RotaDurak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoadBalanceRequest {
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("cardHolderName")
    private String cardHolderName;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("expireMonth")
    private String expireMonth;

    @JsonProperty("expireYear")
    private String expireYear;

    @JsonProperty("cvc")
    private String cvc;

    @JsonProperty("saveCard")
    private boolean saveCard; // kartı kaydet mi?

    @JsonProperty("cardToken")
    private String cardToken;

    @JsonProperty("cardUserKey")
    private String cardUserKey; // daha önce kaydedilmiş kullanıcı anahtarı (varsa)
}
