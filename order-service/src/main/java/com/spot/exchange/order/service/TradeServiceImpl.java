package com.spot.exchange.order.service;

import static com.spot.exchange.order.util.TradeUtil.toBigDecimal;

import com.spot.exchange.order.cache.SnowflakeIdGenerator;
import com.spot.exchange.order.model.domain.Order;
import com.spot.exchange.order.model.domain.OrderAction;
import com.spot.exchange.order.model.domain.OrderState;
import com.spot.exchange.order.model.request.CancelOrderParam;
import com.spot.exchange.order.model.request.PlaceOrderParam;
import com.spot.exchange.order.model.response.OrderVo;
import com.spot.exchange.order.model.response.ResultVO;
import com.spot.exchange.order.repo.OrderRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TradeServiceImpl implements TradeService {

  @Autowired
  OrderService orderService;

  @Autowired
  OrderRepository orderRepository;

  @Autowired
  ModelMapper mapper;

  SnowflakeIdGenerator orderIdGenerator = new SnowflakeIdGenerator(1, 1);
  SnowflakeIdGenerator requestIdGenerator = new SnowflakeIdGenerator(1, 1);

  @Override
  public ResultVO<OrderVo> placeOrder(PlaceOrderParam placeOrderParam) {
    ResultVO<OrderVo> result = ResultVO.success();

    // Filter the order correctness
    if (!orderService.validatePlaceOrderParams(placeOrderParam)) {
      result.setMsg(String.format("The order placed by  %s is not valid hence cannot be placed",
          placeOrderParam.getUserId()));
      return result;
    }
    return createOrderObjectAndPlaceOrder(placeOrderParam);
  }

  @Override
  public ResultVO<OrderVo> cancelOrder(CancelOrderParam cancelOrderParam) {
    ResultVO<OrderVo> result = ResultVO.success();

    Order order = orderRepository.findByOrderId(cancelOrderParam.getOrdId());
    if (order == null) {
      result.setMsg(String.format(
          "Order with order Id %s not available please check and try cancel order again ",
          cancelOrderParam.getOrdId()));
      return result;
    }
    // Assumption
    if (!OrderState.PENDING.equals(order.getState())) {
      result.setMsg(String.format("The order with OrderId %s is already ", order.getState()));
      return result;
    }
    orderService.submitOrderRequestToQueue(order, OrderAction.CANCEL_ORDER);
    result.setMsg(
        String.format("The order with OrderId %s is successfully requested for cancellation ",
            order.getOrderId()));
    return result;
  }

  private ResultVO<OrderVo> createOrderObjectAndPlaceOrder(PlaceOrderParam placeOrderParam) {
    ResultVO<OrderVo> result = ResultVO.success();
    Order order = Order.builder()
        .id(requestIdGenerator.nextId())
        .createdAt(placeOrderParam.getCreatedAt())
        .requestId(orderIdGenerator.nextId())
        .side(placeOrderParam.getSide())
        .timestamp(System.currentTimeMillis())
        .orderId(orderIdGenerator.nextId())
        .quantity(placeOrderParam.getQuantity())
        .instId(placeOrderParam.getInstId())
        .limitPrice(toBigDecimal(placeOrderParam.getLimitPrice()))
        .userId(placeOrderParam.getUserId())
        .state(OrderState.PENDING)
        .build();
    log.info("Place Order : Placed the order is {}", order);
    order = orderRepository.save(order);
    orderService.submitOrderRequestToQueue(order, OrderAction.PLACE_ORDER);
    result.setMsg("Place order request submitted successfully");
    result.setData(List.of(mapper.map(order, OrderVo.class)));
    return result;
  }
}
