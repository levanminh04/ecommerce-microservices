package com.ecommerce.order.RabbitMqConfig;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;

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


//    Khi RabbitAdmin được khởi tạo, (phương thức afterPropertiesSet hoặc initialize). Nó sẽ làm hành động sau:
//    "Này Spring Context! Hãy liệt kê cho tôi tất cả các Bean có kiểu là Queue, Exchange, và Binding đang có trong bộ nhớ!"
//    Spring Context đưa cho RabbitAdmin danh sách các bean đã tạo (queue(), topicExchange(),...).
//    Lúc này, RabbitAdmin dùng cái ConnectionFactory (mà nó nắm giữ) để gọi lên Server RabbitMQ thật
//    Tham số ConnectionFactory: Khi Spring khởi tạo hàm này, nó tự động tìm  ConnectionFactory (đã được Spring Boot tự cấu hình dựa trên host/port/username/password trong file properties) và ném vào đây.
//    Đây là chìa khóa để mở cửa vào RabbitMQ Server.
    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean // RabbitMQ chỉ hiểu byte (mảng byte). Java dùng Object (ví dụ OrderDTO).
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setExchange(exchangeName);
        return template;
    }
    // RabbitTemplate không phải là một kết nối. Nó là một Facade Pattern (Mặt tiền) che giấu sự phức tạp của việc quản lý kết nối, kênh (channel) và gửi tin.
    //
    //


}
