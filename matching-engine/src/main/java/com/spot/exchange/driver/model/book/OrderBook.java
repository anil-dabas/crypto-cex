package com.spot.exchange.driver.model.book;

import com.spot.exchange.driver.model.payload.OrderRequestPayload;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.TreeMap;

public class OrderBook {

  private final NavigableMap<Integer, Queue<OrderRequestPayload>> buyOrders;
  private final NavigableMap<Integer, Queue<OrderRequestPayload>> sellOrders;

  public OrderBook() {
    buyOrders = new TreeMap<>(Comparator.reverseOrder()); // Highest price first
    sellOrders = new TreeMap<>(); // Lowest price first
  }

  public void addBuyOrder(OrderRequestPayload order) {
    int price = Integer.parseInt(order.getPrice());
    buyOrders.computeIfAbsent(price, k -> new LinkedList<>()).add(order);
  }

  public void addSellOrder(OrderRequestPayload order) {
    int price = Integer.parseInt(order.getPrice());
    sellOrders.computeIfAbsent(price, k -> new LinkedList<>()).add(order);
  }


  public Queue<OrderRequestPayload> getBestBuyOrders() {
    return buyOrders.isEmpty() ? new LinkedList<>() : buyOrders.firstEntry().getValue();
  }

  public Queue<OrderRequestPayload> getBestSellOrders() {
    return sellOrders.isEmpty() ? new LinkedList<>() : sellOrders.firstEntry().getValue();
  }


  public void removeOrder(OrderRequestPayload order) {
    Queue<OrderRequestPayload> queue = buyOrders.get(Integer.parseInt(order.getPrice()));
    if (queue != null) {
      queue.remove(order);
      if (queue.isEmpty()) {
        buyOrders.remove(Integer.parseInt(order.getPrice()));
      }
    }
    queue = sellOrders.get(Integer.parseInt(order.getPrice()));
    if (queue != null) {
      queue.remove(order);
      if (queue.isEmpty()) {
        sellOrders.remove(Integer.parseInt(order.getPrice()));
      }
    }
  }
}
