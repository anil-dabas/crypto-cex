package com.spot.exchange.driver.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestPayload {

  private String id;// "xxxxx",
  private int orderAction;// 1- cancel Order, 2 - Place Order, 3 - Amend -order
  private long timestamp;// 1720087432846417648,
  private long orderId;// 2070851335082852352,
  private int side;// "BUY", // or "SELL"
  private int orderType;// "LIMIT", // or "MARKET"
  private String quantity;// "2",
  private String price;// "29",
  private long createdAt;
}
