package com.csci318.microservice.cart.Controllers;

import com.csci318.microservice.cart.Services.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.endpoint.base-url}/cart")
public class CartController {

    private final CartService cartService;


    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
}
