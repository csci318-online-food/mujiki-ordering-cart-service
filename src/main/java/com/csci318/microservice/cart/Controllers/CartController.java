package com.csci318.microservice.cart.Controllers;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Entities.Cart;
import com.csci318.microservice.cart.Entities.CartItem;
import com.csci318.microservice.cart.Entities.Relation.Order;
import com.csci318.microservice.cart.Entities.Relation.Payment;
import com.csci318.microservice.cart.Services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    // Domain Service
    @PostMapping("/{cartId}/items")
    public ResponseEntity<Cart> addItemToCart(@PathVariable UUID cartId, @RequestBody CartItemDTORequest cartItemRequest) {
        Cart cartDTOResponse = cartService.addItemToCart(cartId, cartItemRequest);
        return ResponseEntity.ok(cartDTOResponse);
    }

    @PostMapping("/process-order/{cartId}")
    public ResponseEntity<Order> processOrder(@PathVariable UUID cartId, @RequestParam(name="paymentId") UUID paymentId) {
        Order cartDTOResponse = cartService.createOrder(cartId, paymentId);
        return ResponseEntity.ok(cartDTOResponse);
    }

    @GetMapping("/payments/user/{userId}")
    public ResponseEntity<List<Payment>> getAllPaymentsFromUser(@PathVariable UUID userId) {
        List<Payment> payments = cartService.getAllPaymentsFromUser(userId);
        return ResponseEntity.ok(payments);
    }
}
