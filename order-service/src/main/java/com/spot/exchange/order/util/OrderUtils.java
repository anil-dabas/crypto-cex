package com.spot.exchange.order.util;

import static com.spot.exchange.order.model.domain.OrderSide.fromValue;

import com.spot.exchange.order.model.domain.Order;
import com.spot.exchange.order.model.payload.UpdateOrderPayload;
import java.time.ZoneOffset;

public class OrderUtils {

  public static UpdateOrderPayload createUpdateOrderPayloadFromOrder(Order order) {
    return UpdateOrderPayload.builder()
        .id(order.getOrderId())
        .userId(order.getUserId())
        .idStr(String.valueOf(order.getOrderId()))
        .createdAt(order.getTimestamp())
        .updatedAt(order.getUpdatedAt().toEpochSecond(ZoneOffset.UTC) * 1000)
        .symbol(order.getInstId())
        .side(fromValue(order.getSide()))
        .price(order.getLimitPrice() != null ? order.getLimitPrice().toString() : "")
        .quantity(order.getQuantity())
        .quoteQuantity(order.getQuoteQuantity())
        .status(order.getState().name())
        .timeInForce(order.getTimeInForce())
        .executedQuantity(order.getExecutedQuantity())
        .executedQuoteQuantity(order.getExecutedQuoteQuantity())
        .build();
  }
}