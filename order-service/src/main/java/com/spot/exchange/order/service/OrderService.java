package com.spot.exchange.order.service;

import com.spot.exchange.order.model.domain.Order;
import com.spot.exchange.order.model.domain.OrderAction;
import com.spot.exchange.order.model.request.PlaceOrderParam;
import com.spot.exchange.order.model.response.TradeResponse;

public interface OrderService {

  void updateMatchedOrderDetailsAndPublishResponse(TradeResponse tradeResponse);

  void submitOrderRequestToQueue(Order order, OrderAction action);

  boolean validatePlaceOrderParams(PlaceOrderParam placeOrderParam);
}
