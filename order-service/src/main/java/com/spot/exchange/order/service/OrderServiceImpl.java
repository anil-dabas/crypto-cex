package com.spot.exchange.order.service;

import static com.spot.exchange.order.cache.ListedPairsCache.listedPairs;
import static com.spot.exchange.order.util.TradeUtil.convertToMicroseconds;

import com.spot.exchange.order.kafka.KafkaOrderPublisher;
import com.spot.exchange.order.model.domain.Order;
import com.spot.exchange.order.model.domain.OrderAction;
import com.spot.exchange.order.model.payload.OrderRequestPayload;
import com.spot.exchange.order.model.request.PlaceOrderParam;
import com.spot.exchange.order.repo.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

  @Autowired
  KafkaOrderPublisher orderPublisher;

  @Override
  public void submitOrderRequestToQueue(Order order, OrderAction action) {
    // Need to map properly
    OrderRequestPayload payload = OrderRequestPayload.builder().id(order.getRequestId().toString())
        .orderAction(action.getValue()).orderType(order.getOrderType()).orderId(order.getOrderId())
        .side(order.getSide()).quantity(order.getQuantity())
        .createdAt(convertToMicroseconds(order.getCreatedAt()))
        .price(String.valueOf(order.getLimitPrice())).build();
    log.info("Publish to kafka : Instrument id {} OrderPayload {} ", order.getInstId(), payload);
    orderPublisher.publishOrder(payload);
  }

  @Override
  public boolean validatePlaceOrderParams(PlaceOrderParam placeOrderParam) {
    return listedPairs.contains(placeOrderParam.getInstId());
  }
}
