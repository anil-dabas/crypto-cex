package com.spot.exchange.driver.kafka.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spot.exchange.driver.model.response.TradeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradePublisher {

  @Value("${kafka.topic.executed}")
  private String orderExecutedTopic;
  @Value("${kafka.topic.cancelled}")
  private String orderCancelledTopic;

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public TradePublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  public void publishExecutedTrade(TradeResponse tradeResponse) {
    try {
      String message = objectMapper.writeValueAsString(tradeResponse);
      kafkaTemplate.send(orderExecutedTopic, message);
      log.debug("Publishing Trade Response to Kafka: {}", message);
    } catch (JsonProcessingException e) {
      log.error("Error while serializing Order data: {}", e.getMessage());
    }
  }

  public void publishCancelledTrade(TradeResponse tradeResponse) {
    try {
      String message = objectMapper.writeValueAsString(tradeResponse);
      kafkaTemplate.send(orderCancelledTopic, message);
      log.debug("Publishing Trade Response to Kafka: {}", message);
    } catch (JsonProcessingException e) {
      log.error("Error while serializing Order data: {}", e.getMessage());
    }
  }
}
