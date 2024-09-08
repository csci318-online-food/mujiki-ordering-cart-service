package com.csci318.microservice.cart.Controllers;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${api.endpoint.base-url}/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartDTOResponse> createCart(@RequestBody CartDTORequest cartDTORequest) {
        CartDTOResponse cartDTOResponse = cartService.createCart(cartDTORequest);
        return ResponseEntity.ok(cartDTOResponse);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartDTOResponse> addItemToCart(@PathVariable UUID cartId, @RequestBody CartItemDTORequest cartItemRequest) {
        CartDTOResponse cartDTOResponse = cartService.addItemToCart(cartId, cartItemRequest);
        return ResponseEntity.ok(cartDTOResponse);
    }
}
