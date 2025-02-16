package com.spot.exchange.driver.engine;

import com.lmax.disruptor.EventHandler;
import com.spot.exchange.driver.kafka.publisher.TradePublisher;
import com.spot.exchange.driver.model.book.OrderBook;
import com.spot.exchange.driver.model.payload.OrderRequestPayload;
import com.spot.exchange.driver.model.response.TradeResponse;
import java.util.Iterator;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.spot.exchange.driver.model.event.OrderEvent;

@Slf4j
@Component
public class OrderHandler implements EventHandler<OrderEvent> {

  @Autowired
  TradePublisher tradePublisher;
  private final OrderBook orderBook = new OrderBook();

  @Override
  public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
    OrderRequestPayload order = event.getOrder();
    log.info("Received order request for sequence {}", sequence);
    try {
      if (order.getOrderAction() == 2) { // Place Order
        if (order.getSide() == 1) { // BUY
          orderBook.addBuyOrder(order);
        } else if (order.getSide() == -1) { // SELL
          orderBook.addSellOrder(order);
        }
        matchOrders();
      } else if (order.getOrderAction() == 1) { // Cancel Order
        cancelOrder(order);
      }
    } catch (Exception e) {
      log.error("Error processing order: {}", e.getMessage(), e);
    }finally {
      event.clear(); // Ensure the event is cleared even if an exception occurs
    }

  }

  private void matchOrders() {
    while (true) {
      Queue<OrderRequestPayload> buyOrders = orderBook.getBestBuyOrders();
      Queue<OrderRequestPayload> sellOrders = orderBook.getBestSellOrders();

      if (buyOrders.isEmpty() || sellOrders.isEmpty()) {
        return;
      }
      Iterator<OrderRequestPayload> buyIterator = buyOrders.iterator();
      Iterator<OrderRequestPayload> sellIterator = sellOrders.iterator();

      while (buyIterator.hasNext() && sellIterator.hasNext()) {
        OrderRequestPayload buyOrder = buyIterator.next();
        OrderRequestPayload sellOrder = sellIterator.next();

        if (Integer.parseInt(buyOrder.getPrice()) < Integer.parseInt(sellOrder.getPrice())) {
          return;
        }
        double buyQuantity = Integer.parseInt(buyOrder.getQuantity());
        double sellQuantity = Integer.parseInt(sellOrder.getQuantity());
        double matchedQuantity = Math.min(buyQuantity, sellQuantity);
        executeTrade(buyOrder, sellOrder, matchedQuantity);

        if (buyQuantity > sellQuantity) {
          buyOrder.setQuantity(String.valueOf(buyQuantity - matchedQuantity));
          sellIterator.remove();
        } else if (sellQuantity > buyQuantity) {
          sellOrder.setQuantity(String.valueOf(sellQuantity - matchedQuantity));
          buyIterator.remove();
        } else {
          buyIterator.remove();
          sellIterator.remove();
        }
      }
    }
  }

  private void executeTrade(OrderRequestPayload buyOrder, OrderRequestPayload sellOrder,
      double matchedQuantity) {
    TradeResponse tradeResponse = TradeResponse.builder()
        .id(System.currentTimeMillis())
        .symbol("BTC-USDT")
        .buy(buyOrder.getOrderId())
        .sell(sellOrder.getOrderId())
        .quantity(String.valueOf(matchedQuantity))
        .price(buyOrder.getPrice())
        .buyerMaker(true)
        .timestamp(System.currentTimeMillis())
        .build();
    //log.info("Executed trade at {} for buy order {}, sell order {}", System.currentTimeMillis(),buyOrder.getOrderId(), sellOrder.getOrderId());
    tradePublisher.publishExecutedTrade(tradeResponse);
  }

  private void cancelOrder(OrderRequestPayload event) {
    orderBook.removeOrder(event);
    //log.info("Cancelled order at {}", System.currentTimeMillis());
    TradeResponse cancellationResponse = TradeResponse.builder()
        .id(event.getOrderId())
        .symbol("BTC-USDT")
        .quantity("0")
        .price("0")
        .buyerMaker(false)
        .timestamp(System.currentTimeMillis())
        .build();
    tradePublisher.publishCancelledTrade(cancellationResponse);
  }

}
