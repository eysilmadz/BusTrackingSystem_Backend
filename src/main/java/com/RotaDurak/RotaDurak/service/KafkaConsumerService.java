package com.RotaDurak.RotaDurak.service;

import com.RotaDurak.RotaDurak.dto.PositionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @Autowired
    private SimpMessagingTemplate messaging;

    @Autowired
    public KafkaConsumerService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @KafkaListener(topics = "bus-positions",groupId = "bus-websocket")
    public void receive(PositionMessage message) {
        //gelen koordinatları websocket ile client'lara yayın
        messaging.convertAndSend("/topic/route/"+message.getRouteId(), message);
    }
}
