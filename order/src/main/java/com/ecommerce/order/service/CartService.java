package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.client.UserServiceClient;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.model.CartItem;

import com.ecommerce.order.repository.CartItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartItemRepository cartItemRepository;

    private final ProductServiceClient productServiceClient;

    private final UserServiceClient userServiceClient;


//    Annotation này hoạt động như 1 Proxy bao quanh phương thức, Mỗi khi phương thức này được gọi,
//    Resilience4j sẽ ghi nhận kết quả: thành công hay thất bại (ném ra Exception).
//    Nếu mạch đang OPEN, khi gọi addToCart, Resilience4j sẽ ném ngoại lệ (thường là CallNotPermittedException) mà không hề chạy bất kỳ dòng code nào bên trong hàm này.
//    Nếu trả về null hoặc false: Resilience4j coi là một Success. Mạch sẽ không tính đây là lỗi vì về mặt kỹ thuật, hàm vẫn chạy xong và trả về giá trị.
//    Nếu Throw Exception: Mặc định, bất kỳ ngoại lệ nào (RuntimeException, Exception...) được ném ra khỏi hàm addToCart mà không được catch lại bên trong hàm đó sẽ được tính là Thất bại (Fail).
//    Dù lỗi phát sinh từ productServiceClient, userServiceClient, hay thậm chí là lỗi ở dòng cartItemRepository.save(), thì Resilience4j đều tính chung vào một "giỏ" lỗi.
    @CircuitBreaker(name = "productService", fallbackMethod = "addToCartFallBack") // đây là circuit breaker ở mức method (cụ thể trong 1 service luôn) tuy nhiên đây không phải best practice,  nên citcuit breaker tại gateway
    public boolean addToCart(String userId, CartItemRequest request) {


        ProductResponse productResponse = productServiceClient.getProductDetails(Long.valueOf(request.getProductId()));
        if (productResponse == null)
            return false;
        if (productResponse.getStockQuantity() < request.getQuantity())
            return false;

        UserResponse userDetails = userServiceClient.getUserDetails(userId);
        if (userDetails == null)
            return false;


        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        if (existingCartItem != null) {
            // Update the quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
            existingCartItem.setPrice(BigDecimal.valueOf(10000));
            cartItemRepository.save(existingCartItem);
        } else {
            // Create new cart item
           CartItem cartItem = new CartItem();
           cartItem.setUserId(userId);
           cartItem.setProductId(request.getProductId());
           cartItem.setQuantity(request.getQuantity());
           cartItem.setPrice(BigDecimal.valueOf(10000));
           cartItemRepository.save(cartItem);
        }
        return true;
    }

    public boolean deleteItemFromCart(String userId, String productId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId,productId);

        if (cartItem != null){
            cartItemRepository.delete(cartItem);
            return true;
        }
        return false;
    }

    public List<CartItem> getCart(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);

    }

    public boolean addToCartFallBack(String userId, CartItemRequest request, Exception exception){
        System.out.println("addToCartFallBack Called");
        exception.printStackTrace();
        return false;
    }

}
