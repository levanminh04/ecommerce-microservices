package com.ecommerce.order.config;

import com.ecommerce.order.client.ProductServiceClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

@Configuration
public class ProductServiceClientConfig {


    @Bean // Ở đây Spring sẽ tự động tìm cái Bean restClientBuilder (cái có @LoadBalanced) để tiêm vào tham số này.
    public ProductServiceClient productRestClientInterface(RestClient.Builder restClientBuilder){
        RestClient restClient = restClientBuilder
                .baseUrl("http://product-service") // Nhờ có @LoadBalanced ở bước 1, Spring mới hiểu được cái tên này.
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        (request, response) -> Optional.empty())
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        ProductServiceClient productServiceClient = factory.createClient(ProductServiceClient.class);

        return productServiceClient;

    }
}
