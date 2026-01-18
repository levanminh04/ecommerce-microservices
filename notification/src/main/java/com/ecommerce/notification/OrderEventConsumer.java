package com.ecommerce.notification;

import com.ecommerce.notification.DTO.OrderCreatedEvent;
import com.ecommerce.notification.DTO.OrderItemDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.awt.geom.QuadCurve2D;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class OrderEventConsumer {
//    @RabbitListener(queues = "${rabbitmq.queue.name}")
//    public void handleOrderEvent(OrderCreatedEvent event){
//        System.out.println("check: " + event.getOrderId());
//    }
    @Bean
    public Consumer<OrderCreatedEvent> orderCreated() {
        return event -> {
            System.out.println("Received order created event for order: " + event.getOrderId());
            System.out.println("Received order created event for user id: "+ event.getUserId());
        };
    }

}
