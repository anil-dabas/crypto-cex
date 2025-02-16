package com.spot.exchange.driver.model.event;

import com.spot.exchange.driver.model.payload.OrderRequestPayload;

public class OrderEvent {

  private OrderRequestPayload order;

  public OrderRequestPayload getOrder() {
    return order;
  }
  public void setOrder(OrderRequestPayload order) {
    this.order = order;
  }
  public void clear() {
    this.order = null;  // Clear the reference after processing
  }
}
