package com.ecommerce.order.clientConfig;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    @LoadBalanced // gắn @LoadBalanced cho RestClient để tận dụng tính năng của eureka
    public RestClient.Builder restClientBuilder(){
        return RestClient.builder();
    }
}
