package com.csci318.microservice.cart.DTOs;

import java.util.UUID;

public class CartItemDTORequest {
    private UUID cartId;
    private UUID itemId;
    private int quantity;
    private Double price;
}
