package com.spot.exchange.driver.config;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.spot.exchange.driver.engine.OrderHandler;
import com.spot.exchange.driver.model.payload.OrderRequestPayload;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisruptorConfig {

    @Bean
    public Disruptor<OrderRequestPayload> disruptor(OrderHandler orderHandler) {
        EventFactory<OrderRequestPayload> factory = OrderRequestPayload::new;
        int bufferSize = 1024; // buffer size should be power of 2
        Executor executor = Executors.newCachedThreadPool();
        Disruptor<OrderRequestPayload> disruptor = new Disruptor<>(factory, bufferSize, executor);
        disruptor.handleEventsWith(orderHandler);
        return disruptor;
    }
}
