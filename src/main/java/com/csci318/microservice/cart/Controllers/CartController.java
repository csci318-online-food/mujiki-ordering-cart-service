package com.csci318.microservice.cart.Controllers;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Domain.Relations.Order;
import com.csci318.microservice.cart.Domain.Relations.Payment;
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

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartDTOResponse> addItemToCart(@PathVariable UUID cartId, @RequestBody CartItemDTORequest cartItemRequest) {
        CartDTOResponse cartDTOResponse = cartService.addItemToCart(cartId, cartItemRequest);
        return ResponseEntity.ok(cartDTOResponse);
    }

    @PostMapping("/process-order/{cartId}")
    public ResponseEntity<Order> processOrder(@PathVariable UUID cartId,
                                              @RequestParam(name="paymentId") UUID paymentId,
                                              @RequestParam(name = "promotionId") UUID promotionId) {
        Order cartDTOResponse = cartService.createOrder(cartId, paymentId, promotionId);
        return ResponseEntity.ok(cartDTOResponse);
    }

    @GetMapping("/payments/user/{userId}")
    public ResponseEntity<List<Payment>> getAllPaymentsFromUser(@PathVariable UUID userId) {
        List<Payment> payments = cartService.getAllPaymentsFromUser(userId);
        return ResponseEntity.ok(payments);
    }
}
