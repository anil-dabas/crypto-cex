package com.spot.exchange.order.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spot.exchange.order.model.payload.OrderRequestPayload;
import com.spot.exchange.order.model.payload.UpdateOrderPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaOrderPublisher {

    @Value("${kafka.topic.order}")
    private String orderTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaOrderPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrder(OrderRequestPayload order) {
        try {
            String message = objectMapper.writeValueAsString(order);
            kafkaTemplate.send(orderTopic, message);
            log.debug("Publishing Order to Kafka: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error while serializing Order data: {}", e.getMessage());
        }
    }

}
