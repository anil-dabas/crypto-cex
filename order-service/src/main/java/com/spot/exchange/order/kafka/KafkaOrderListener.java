package com.spot.exchange.order.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spot.exchange.order.model.response.TradeResponse;
import com.spot.exchange.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaOrderListener {

  @Autowired
  OrderService orderService;

  private final ObjectMapper objectMapper;

  public KafkaOrderListener() {
    this.objectMapper = new ObjectMapper();
  }

  @KafkaListener(topics = "${kafka.topic.orderstatus.matched}", groupId = "order_group")
  public void listenMatchedOrdersUpdate(String message) {
    try {
      TradeResponse tradeResponse = objectMapper.readValue(message, TradeResponse.class);
      //orderService.updateMatchedOrderDetailsAndPublishResponse(tradeResponse);
      log.info("Received trade response: {}", tradeResponse);
    } catch (JsonProcessingException e) {
      log.info("Error processing message: {}", e.getMessage());
    }

  }
}