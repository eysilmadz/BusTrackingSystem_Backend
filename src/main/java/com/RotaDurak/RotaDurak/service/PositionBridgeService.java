package com.RotaDurak.RotaDurak.service;

import com.RotaDurak.RotaDurak.dto.PositionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PositionBridgeService {
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public PositionBridgeService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "bus-positions",
            groupId = "bus-websocket",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void bridge(PositionMessage message) {
        //Gelen her konumu ilgili STOMP topic'ine gönder
        messagingTemplate.convertAndSend("/topic/route/" + message.getRouteId(), message);
    }
}
