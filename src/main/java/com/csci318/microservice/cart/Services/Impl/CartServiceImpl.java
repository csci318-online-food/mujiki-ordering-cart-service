package com.csci318.microservice.cart.Services.Impl;

import com.csci318.microservice.cart.Repositories.CartRepository;
import com.csci318.microservice.cart.Services.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final RestTemplate restTemplate;
    private final CartRepository cartRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${address.url.service}")
    private String ADDRESS_URL;

    public CartServiceImpl(RestTemplate restTemplate, CartRepository cartRepository, ApplicationEventPublisher eventPublisher) {
        this.restTemplate = restTemplate;
        this.cartRepository = cartRepository;
        this.eventPublisher = eventPublisher;
    }

    // USE CASE: ADD ITEM TO CART
    // STEP 1: Create Cart object
    // STEP 2: Add item to cart
//    public CartDTOResponse createCart(Cart cart) {
//        return cartRepository.save(cart);
//    }

}
