package com.ecommerce.notification;

import com.ecommerce.notification.DTO.OrderCreatedEvent;
import com.ecommerce.notification.DTO.OrderItemDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.awt.geom.QuadCurve2D;
import java.util.List;
import java.util.Map;

@Service
public class OrderEventConsumer {
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderEvent(OrderCreatedEvent event){
        System.out.println("check: " + event);
    }


}
