package com.spot.exchange.driver.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.RingBuffer;
import com.spot.exchange.driver.model.event.OrderEvent;
import com.spot.exchange.driver.model.payload.OrderRequestPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumer {

  private final ObjectMapper objectMapper;
  private final RingBuffer<OrderEvent> ringBuffer;

  public OrderConsumer(ObjectMapper objectMapper, RingBuffer<OrderEvent> ringBuffer) {
    this.objectMapper = objectMapper;
    this.ringBuffer = ringBuffer;
  }

  @KafkaListener(topics = "${kafka.topic.order}", groupId = "order_group")
  public void consume(String message) {
    try {
      OrderRequestPayload payload = objectMapper.readValue(message, OrderRequestPayload.class);
      long sequence = ringBuffer.next();
      OrderEvent event = ringBuffer.get(sequence);
      event.setOrder(payload);
      ringBuffer.publish(sequence);

    } catch (Exception e) {
      log.error("Error processing message: {}", e.getMessage());
    }
  }
}