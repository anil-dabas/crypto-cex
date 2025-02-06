package com.spot.exchange.driver.engine;

import com.lmax.disruptor.EventHandler;
import com.spot.exchange.driver.kafka.publisher.TradePublisher;
import com.spot.exchange.driver.model.book.OrderBook;
import com.spot.exchange.driver.model.payload.OrderRequestPayload;
import com.spot.exchange.driver.model.response.TradeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderHandler implements EventHandler<OrderRequestPayload> {

  @Autowired
  TradePublisher tradePublisher;
  private final OrderBook orderBook = new OrderBook();

  @Override
  public void onEvent(OrderRequestPayload event, long sequence, boolean endOfBatch) {

    // Process order (match or add to the book)
    if (event.getOrderAction() == 2) { // Place Order
      if (event.getSide() == 1) { // BUY
        orderBook.addBuyOrder(event);
      } else if (event.getSide() == 2) { // SELL
        orderBook.addSellOrder(event);
      }

      // Matching the orders
      matchOrders();
    } else if (event.getOrderAction() == 1) { // Cancel Order
      cancelOrder(event);
    }

  }

  private void matchOrders() {
    OrderRequestPayload buyOrder = orderBook.getBestBuyOrder();
    OrderRequestPayload sellOrder = orderBook.getBestSellOrder();

    if (buyOrder != null && sellOrder != null
        && Double.parseDouble(buyOrder.getPrice()) >= Double.parseDouble(sellOrder.getPrice())) {
      // Execute Trade
      executeTrade(buyOrder, sellOrder);
    }
  }

  private void executeTrade(OrderRequestPayload buyOrder, OrderRequestPayload sellOrder) {
    TradeResponse tradeResponse = TradeResponse.builder()
        .id(System.currentTimeMillis())
        .symbol("BTC-USDT")
        .buy(buyOrder.getOrderId())
        .sell(sellOrder.getOrderId())
        .quantity(buyOrder.getQuantity())
        .price(buyOrder.getPrice())
        .buyerMaker(true)
        .timestamp(System.currentTimeMillis())
        .build();

    // Publish the trade response to Kafka
    tradePublisher.publishExecutedTrade(tradeResponse);

    // Remove matched orders
    orderBook.removeOrder(buyOrder);
    orderBook.removeOrder(sellOrder);
  }

  private void cancelOrder(OrderRequestPayload event) {
    if (event.getSide() == 1) { // BUY
      orderBook.removeOrder(event);
    } else if (event.getSide() == 2) { // SELL
      orderBook.removeOrder(event);
    }

    TradeResponse cancellationResponse = TradeResponse.builder()
        .id(event.getOrderId()) // Use the order ID as the unique trade ID
        .symbol("BTC-USDT")
        .quantity("0") // No quantity for canceled orders
        .price("0") // No price for canceled orders
        .buyerMaker(false) // Not relevant for cancel action
        .timestamp(System.currentTimeMillis())
        .build();

    tradePublisher.publishCancelledTrade(cancellationResponse);
  }
}
