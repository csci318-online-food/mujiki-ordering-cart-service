package com.csci318.microservice.cart.Services;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Domain.Relations.Order;
import com.csci318.microservice.cart.Domain.Relations.Payment;

import java.util.List;
import java.util.UUID;

public interface CartService {
    CartDTOResponse createCart(CartDTORequest cartDTORequest);
    CartDTOResponse addItemToCart(UUID cartId, CartItemDTORequest cartItemRequest);
    Order createOrder(UUID cartId, UUID paymentId, UUID promotionId);
    List<Payment> getAllPaymentsFromUser(UUID userId);
}
