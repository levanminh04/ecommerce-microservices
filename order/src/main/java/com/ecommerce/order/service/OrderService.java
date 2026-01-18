package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderCreatedEvent;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.*;
import com.ecommerce.order.repository.OrderRepository;
import io.netty.util.concurrent.OrderedEventExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartService cartService;
    private final OrderRepository orderRepository;
//    private final RabbitTemplate rabbitTemplate;
//
//    @Value("${rabbitmq.queue.name}")
//    private String queueName;
//
//    @Value("${rabbitmq.exchange.name}")
//    private String exchangeName;
//
//    @Value("${rabbitmq.routing.key}")
//    private String routingKey;
    private final StreamBridge streamBridge;

    public Optional<OrderResponse> createOrder(String userId) {
//        // Validate for cart items
        List<CartItem> cartItems = cartService.getCart(userId);
        if (cartItems.isEmpty()) {
            return Optional.empty();
        }
//        // Validate for user
//
//        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
//        if (userOptional.isEmpty()) {
//            return Optional.empty();
//        }
//        User user = userOptional.get();

        // Calculate total price
        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> new OrderItem(
                        null,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        order
                ))
                .toList();

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // Clear the cart
        cartService.clearCart(userId);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getStatus(),
                mapToOrderItemDTO(savedOrder.getItems()),
                savedOrder.getTotalAmount(),
                savedOrder.getCreatedAt()
        );

//        rabbitTemplate.convertAndSend(exchangeName,
//                                      routingKey,
//                                      event);

        streamBridge.send("createOrder-out-0", event);
// Trong thực tế phát triển, StreamBridge được sử dụng phổ biến hơn rất nhiều so với Supplier để gửi message.
// Đây là lý do: Supplier mặc định hoạt động theo cơ chế Polling (định kỳ tự chạy, ví dụ mỗi 1 giây chạy 1 lần).
// Nó không biết khi nào người dùng gọi API. Để nối API với Supplier, bạn phải dùng các kỹ thuật Reactive phức tạp,
// làm code trở nên rối rắm không cần thiết.  Nó cho phép bạn gửi message "theo mệnh lệnh" (Imperative).
// Bạn có thể gọi nó ở bất cứ đâu (Controller, Service) ngay khi bạn cần.

        return Optional.of(mapToOrderResponse(savedOrder));


    }

    public List<OrderItemDTO> mapToOrderItemDTO(List<OrderItem> items){
        return items.stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).collect(Collectors.toUnmodifiableList());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getItems().stream()
                        .map(orderItem -> new OrderItemDTO(
                                orderItem.getId(),
                                orderItem.getProductId(),
                                orderItem.getQuantity(),
                                orderItem.getPrice(),
                                orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()))
                        ))
                        .toList(),
                order.getCreatedAt()
        );
    }
}
