package com.ecommerce.order.config;

import com.ecommerce.order.client.UserServiceClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

@Configuration
public class UserServiceClientConfig {

//    @Bean // Spring thấy annotation @Bean. Nó sẽ chạy hàm này, lấy đối tượng RestClient.Builder được trả về và cất vào "kho" của nó.
//    @LoadBalanced
//    RestClient.Builder restClientBuilder() {
//        return RestClient.builder();
//    }

    // Khi Spring chuẩn bị tạo Bean ProductServiceClient, nó thấy hàm này yêu cầu một RestClient.Builder.
    // Nó lục trong kho, thấy có một ông Builder (đã tạo ở Bước 1) khớp kiểu dữ liệu, nó sẽ tự động lấy và truyền vào tham số restClientBuilder cho bạn.
    @Bean
    public UserServiceClient uerRestClientInterface(RestClient.Builder restClientBuilder){

        RestClient restClient = restClientBuilder
                .baseUrl("http://user-service")
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        ((request, response) -> Optional.empty()))
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        UserServiceClient userServiceClient = factory.createClient(UserServiceClient.class);
        return userServiceClient;



    }



}
