package com.spot.exchange.driver.model.book;

import com.spot.exchange.driver.model.payload.OrderRequestPayload;

import java.util.PriorityQueue;

public class OrderBook {

    private final PriorityQueue<OrderRequestPayload> buyOrders;
    private final PriorityQueue<OrderRequestPayload> sellOrders;

    public OrderBook() {
        buyOrders = new PriorityQueue<>((o1, o2) -> Double.compare(Double.parseDouble(o2.getPrice()), Double.parseDouble(o1.getPrice()))); // highest price first
        sellOrders = new PriorityQueue<>((o1, o2) -> Double.compare(Double.parseDouble(o1.getPrice()), Double.parseDouble(o2.getPrice()))); // lowest price first
    }

    public void addBuyOrder(OrderRequestPayload order) {
        buyOrders.add(order);
    }

    public void addSellOrder(OrderRequestPayload order) {
        sellOrders.add(order);
    }

    public OrderRequestPayload getBestBuyOrder() {
        return buyOrders.peek();
    }

    public OrderRequestPayload getBestSellOrder() {
        return sellOrders.peek();
    }

    public void removeOrder(OrderRequestPayload order) {
        buyOrders.remove(order);
        sellOrders.remove(order);
    }
}
