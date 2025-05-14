package com.RotaDurak.RotaDurak.service;

import com.RotaDurak.RotaDurak.dto.PositionMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private static final String TOPIC = "bus-positions";

    private final ProducerFactory<String, PositionMessage> producerFactory;

    @Autowired
    public KafkaProducerService (ProducerFactory<String, PositionMessage> producerFactory) {
        this.producerFactory = producerFactory;
    }

    public void send(PositionMessage message) {
        //yeni bir producer al
        Producer<String,PositionMessage> producer = producerFactory.createProducer();
        try{
            ProducerRecord<String,PositionMessage> record =
                    new ProducerRecord<>(TOPIC, message.getRouteId().toString(), message);
            producer.send(record);
        } finally {
            //işiimiz bitti kapatabiliriz
            producer.close();
        }
    }






    /**
     * @Autowired
     * private KafkaTemplate<String, PositionMessage> kafkaTemplate;
     * public void send(PositionMessage message) {
     * kafkaTemplate.send(TOPIC, message.getRouteId().toString(), message);
     *}
     **/


}
