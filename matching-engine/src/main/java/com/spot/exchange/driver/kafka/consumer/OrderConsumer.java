package com.spot.exchange.driver.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spot.exchange.driver.engine.OrderHandler;
import com.spot.exchange.driver.model.payload.OrderRequestPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumer {

    private final ObjectMapper objectMapper;
    private final OrderHandler orderHandler;

    public OrderConsumer(ObjectMapper objectMapper, OrderHandler orderHandler) {
        this.objectMapper = objectMapper;
      this.orderHandler = orderHandler;
    }
    @KafkaListener(topics = "${kafka.topic.order}", groupId = "order_group")
    public void consume(String message) {
        try {
            OrderRequestPayload payload = objectMapper.readValue(message, OrderRequestPayload.class);
            orderHandler.onEvent(payload,0,true);
        } catch (JsonProcessingException e) {
            log.info("Error processing message: {}", e.getMessage());
        }
    }
}