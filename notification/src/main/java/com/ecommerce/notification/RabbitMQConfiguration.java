package com.ecommerce.notification;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;


    // Bean Queue chỉ là TỜ GIẤY THIẾT KẾ, Nó chỉ chứa thông tin: "Tôi muốn một hàng đợi tên là "${rabbitmq.queue.name}", bền vững".
    @Bean
    public Queue queue(){
        return QueueBuilder.durable(queueName)
                .build();
    }
    // Bean TopicExchange chỉ là TỜ GIẤY THIẾT KẾ
    @Bean
    public TopicExchange topicExchange(){
        return ExchangeBuilder.topicExchange(exchangeName)
                .durable(true)
                .build();
    }
    // Bean Binding chỉ là TỜ GIẤY THIẾT KẾ
    // Ở đây gọi hàm queue() và topicExchange(). Trong Java thường, gọi hàm là tạo mới object.
    // Nhưng vì có @Configuration, Spring sử dụng kỹ thuật CGLIB proxy.
    // Khi gọi queue(), Spring thông minh chặn lại kiểm tra: "Cái Bean Queue này tạo chưa? Nếu tạo rồi thì trả về cái đang có trong bộ nhớ, không tạo mới."
    // -> Đảm bảo tính nhất quán (Singleton).
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue())
                .to(topicExchange())
                .with(routingKey); // Topic Exchange
    }


    @Bean // RabbitMQ chỉ hiểu byte (mảng byte). Java dùng Object (ví dụ OrderDTO).
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }



}
