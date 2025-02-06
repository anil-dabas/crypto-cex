package com.spot.exchange.order.service;

import static com.spot.exchange.order.cache.ListedPairsCache.listedPairs;
import static com.spot.exchange.order.model.domain.OrderState.PARTIALLY_FILLED;
import static com.spot.exchange.order.util.OrderUtils.createUpdateOrderPayloadFromOrder;
import static com.spot.exchange.order.util.TradeUtil.convertMicrosecondsToLocalDateTime;
import static com.spot.exchange.order.util.TradeUtil.convertToMicroseconds;
import static com.spot.exchange.order.util.TradeUtil.toBigDecimal;
import static com.spot.exchange.order.util.TradeUtil.toStringFromBigDecimal;

import com.spot.exchange.order.kafka.KafkaOrderPublisher;
import com.spot.exchange.order.model.domain.Order;
import com.spot.exchange.order.model.domain.OrderAction;
import com.spot.exchange.order.model.payload.OrderRequestPayload;
import com.spot.exchange.order.model.payload.UpdateOrderPayload;
import com.spot.exchange.order.model.request.PlaceOrderParam;
import com.spot.exchange.order.model.response.TradeResponse;
import com.spot.exchange.order.repo.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

  @Autowired
  KafkaOrderPublisher orderPublisher;


  @Autowired
  OrderRepository orderRepository;

  @Override
  public void updateMatchedOrderDetailsAndPublishResponse(TradeResponse tradeResponse) {

    log.info("Match Order : The trade response received is  {}",tradeResponse);
    Order buyOrder = orderRepository.findByOrderId(tradeResponse.getBuy());
    Order sellOrder = orderRepository.findByOrderId(tradeResponse.getSell());
    log.info("Match Order : The buy order matched is {} & Sell order matched is {}",buyOrder, sellOrder);

    LocalDateTime timeOfExecution = convertMicrosecondsToLocalDateTime(tradeResponse.getTimestamp());

    // Updating buy Order
    buyOrder.setUpdatedAt(timeOfExecution);
    BigDecimal updatedBuyExecuted = toBigDecimal(buyOrder.getExecutedQuantity()).add(toBigDecimal(tradeResponse.getQuantity()));
    buyOrder.setExecutedQuantity(toStringFromBigDecimal(updatedBuyExecuted));
    buyOrder.setState(PARTIALLY_FILLED);

    // Updating sell Order
    sellOrder.setUpdatedAt(timeOfExecution);
    BigDecimal updatedSellExecuted = toBigDecimal(sellOrder.getExecutedQuantity()).add(toBigDecimal(tradeResponse.getQuantity()));
    sellOrder.setExecutedQuantity(toStringFromBigDecimal(updatedSellExecuted));
    sellOrder.setState(PARTIALLY_FILLED);

    // Save orders
    log.info("Match Order : Saving updated orders in the order table");
    orderRepository.saveAll(List.of(buyOrder,sellOrder));

    // Convert to UpdatedOrderPayload Buy and Sell Order
    UpdateOrderPayload updateBuyOrderPayload = createUpdateOrderPayloadFromOrder(buyOrder);
    UpdateOrderPayload updateSellOrderPayload = createUpdateOrderPayloadFromOrder(sellOrder);

    log.info("Match Order : Publishing buy order to update order kafka {}",updateBuyOrderPayload);
    orderPublisher.publishOrderUpdate(updateBuyOrderPayload);
    log.info("Match Order : Publishing sell order to update order kafka {}",updateSellOrderPayload);
    orderPublisher.publishOrderUpdate(updateSellOrderPayload);

  }

  @Override
  public void submitOrderRequestToQueue(Order order, OrderAction action) {
    // Need to map properly
    OrderRequestPayload payload = OrderRequestPayload.builder().id(order.getRequestId().toString())
        .orderAction(action.getValue()).orderType(order.getOrderType()).orderId(order.getOrderId())
        .side(order.getSide()).quantity(order.getQuantity()).quoteQuantity(order.getQuoteQuantity())
        .createdAt(convertToMicroseconds(order.getCreatedAt()))
        .price(toStringFromBigDecimal(order.getLimitPrice())).build();
    log.info("Publish to kafka : Instrument id {} OrderPayload {} ", order.getInstId(), payload);
    orderPublisher.publishOrder(payload, order.getInstId());
  }

  @Override
  public boolean validatePlaceOrderParams(PlaceOrderParam placeOrderParam) {
    return listedPairs.contains(placeOrderParam.getInstId());
  }
}
