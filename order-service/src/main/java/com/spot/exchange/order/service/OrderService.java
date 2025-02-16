package com.spot.exchange.order.service;

import com.spot.exchange.order.model.domain.Order;
import com.spot.exchange.order.model.domain.OrderAction;
import com.spot.exchange.order.model.request.PlaceOrderParam;

public interface OrderService {

  void submitOrderRequestToQueue(Order order, OrderAction action);

  boolean validatePlaceOrderParams(PlaceOrderParam placeOrderParam);
}
