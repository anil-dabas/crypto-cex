package com.spot.exchange.driver;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DriverService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8084/api/v1/trade";

    private final Map<String, List<OrderInfo>> userOrders = new HashMap<>();

    public DriverService() {
    }

    public void executeOrderSequence() {
        // Generate random prices
        Random rand = new Random();

        // Step 1: Place Orders and Save Order IDs with userId
        Long order1 = placeOrder("user4", "buy", rand.nextInt(440) + 500, 45);
        Long order2 = placeOrder("user3", "buy", rand.nextInt(100) + 950, 15);
        Long order3 = placeOrder("user1", "buy", rand.nextInt(300) + 1200, 20);
        Long order4 = placeOrder("user2", "sell", rand.nextInt(200) + 1100, 10);
        Long order5 = placeOrder("user3", "sell", rand.nextInt(300) + 1100, 15);
        Long order6 = placeOrder("user1", "buy", rand.nextInt(550) + 1250, 5);
        Long order7 = placeOrder("user1", "sell", rand.nextInt(440) + 500, 5);
        Long order8 = placeOrder("user2", "sell", rand.nextInt(650) + 1250, 100);
        Long order9 = placeOrder("user3", "buy", rand.nextInt(150) + 950, 15);
        Long order10 = placeOrder("user4", "sell", rand.nextInt(200) + 1300, 30);

        // Step 2: Cancel Orders
        cancelOrder(order2, "user3");
        cancelOrder(order8, "user2");
        cancelOrder(order10, "user4");
    }

    public Long placeOrder(String userId, String side, int price, int size) {
        String url = baseUrl + "/order";
        OrderRequest request = new OrderRequest("BTC-USDT", side.equals("buy") ? 1 : -1, String.valueOf(size), String.valueOf(price), userId);
        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, createHeaders());
        ResponseEntity<OrderResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, OrderResponse.class);

        // Assuming the orderId is located in the "data" array in the response body
        Long orderId = response.getBody().getData().get(0).getOrderId();
        storeOrder(userId, orderId);  // Store the order ID along with the user ID
        return orderId;
    }

    public void cancelOrder(Long orderId, String userId) {
        String url = baseUrl + "/cancel-order";
        CancelOrderRequest request = new CancelOrderRequest(orderId, "BTC-USDT", userId);
        HttpEntity<CancelOrderRequest> entity = new HttpEntity<>(request, createHeaders());
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    private void storeOrder(String userId, Long orderId) {
        // Store the order ID along with the user ID in memory
        userOrders.putIfAbsent(userId, new ArrayList<>());
        userOrders.get(userId).add(new OrderInfo(orderId, userId));
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @Data
    static class OrderRequest {
        private String instId;
        private int side;
        private String quantity;
        private String limitPrice;
        private String userId;

        public OrderRequest(String instId, int side, String quantity, String limitPrice, String userId) {
            this.instId = instId;
            this.side = side;
            this.quantity = quantity;
            this.limitPrice = limitPrice;
            this.userId = userId;
        }

    }

    @Data
    static class CancelOrderRequest {
        private long ordId;
        private String instId;
        private String userId;

        public CancelOrderRequest(long ordId, String instId, String userId) {
            this.ordId = ordId;
            this.instId = instId;
            this.userId = userId;
        }

    }

    // Response structure for the Place Order API
    static class OrderResponse {
        private int code;
        private String msg;
        @Setter
        @Getter
        private List<OrderData> data;

      @Data
        static class OrderData {
            private long orderId;

        }
    }

    // Helper class to store order info (orderId and userId)
    @Data
    static class OrderInfo {
        private Long orderId;
        private String userId;

        public OrderInfo(Long orderId, String userId) {
            this.orderId = orderId;
            this.userId = userId;
        }

    }
}
