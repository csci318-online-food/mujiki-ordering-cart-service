package com.csci318.microservice.cart.Services;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Entities.Cart;
import com.csci318.microservice.cart.Entities.CartItem;
import com.csci318.microservice.cart.Entities.Relation.Order;
import com.csci318.microservice.cart.Entities.Relation.Payment;
import com.csci318.microservice.cart.Mappers.CartItemMapper;
import com.csci318.microservice.cart.Repositories.CartItemRepository;
import com.csci318.microservice.cart.Repositories.CartRepository;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

public interface CartService {
    CartDTOResponse createCart(CartDTORequest cartDTORequest);
//    CartDTOResponse addItemToCart(UUID cartId, CartItemDTORequest cartItemRequest);
    Cart addItemToCart(UUID cartId, CartItem cartItemRequest);
    Order createOrder (UUID cartId, UUID paymentId);
    List<Payment> getAllPaymentsFromUser(UUID userId);
}
