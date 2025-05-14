package com.RotaDurak.RotaDurak.config;

import com.RotaDurak.RotaDurak.dto.PositionMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, PositionMessage> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        //Kafka broker adresi
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // Key: String, Value: JSON
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, PositionMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, PositionMessage> consumerFactory() {
        // 1) JsonDeserializer’ı routeMessage tipine ayarla
        JsonDeserializer<PositionMessage> jsonDeserializer =
                new JsonDeserializer<>(PositionMessage.class, false);
        jsonDeserializer.addTrustedPackages("*");

        // 2) ErrorHandlingDeserializer sarmalayıcılarını yarat
        ErrorHandlingDeserializer<String> keyDeserializer =
                new ErrorHandlingDeserializer<>(new StringDeserializer());
        ErrorHandlingDeserializer<PositionMessage> valueDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        // 3) Diğer consumer ayarları
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,    "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG,             "bus-websocket");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,    "earliest");
        // Buraya KEY/VALUE_DESERIALIZER_CLASS_CONFIG yazmanıza gerek yok,
        // çünkü onları constructor’la inject ediyoruz.

        // 4) Factory’yi key/value deserializer’larla oluştur
        return new DefaultKafkaConsumerFactory<>(
                props,
                keyDeserializer,
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PositionMessage> kafkaListenerContainerFactory(ConsumerFactory<String,PositionMessage> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, PositionMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // tombstone/null value’ları filtrele (listener’a gitmesin)
        factory.setRecordFilterStrategy(record -> record.value() == null);

        // Hata durumunda önce 2 kez yeniden dene, sonra o kaydı atla
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(1000L, 2)
        );
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
